package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.rss.RssFeed;
import com.github.ayltai.newspaper.rss.RssItem;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

public final class HeadlineClient extends RssClient {
    //region Constants

    public static final String URL = "http://hd.stheadline.com/rss/news/daily/";

    public static final String CATEGORY_HONG_KONG     = "?category=hongkong";
    public static final String CATEGORY_INTERNATIONAL = "?category=international";
    public static final String CATEGORY_CHINA         = "?category=chain";
    public static final String CATEGORY_FINANCE       = "?category=finance";
    public static final String CATEGORY_PROPERTY      = "?category=property";
    public static final String CATEGORY_ENTERTAINMENT = "?category=entertainment";
    public static final String CATEGORY_SUPPLEMENT    = "?category=supplement";
    public static final String CATEGORY_SPORTS        = "?category=sports";

    private static final String IMAGE_URI = "http://static.stheadline.com";
    private static final String HTTP      = "http:";

    //endregion

    private static final Map<String, String> KEYWORDS = new ArrayMap<>(8);

    static {
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_HONG_KONG, " (港聞) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_INTERNATIONAL, " (國際) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_CHINA, " (中國) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_FINANCE, " (財經) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_PROPERTY, " (地產) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_ENTERTAINMENT, " (娛樂) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_SUPPLEMENT, " (副刊) ");
        HeadlineClient.KEYWORDS.put(HeadlineClient.CATEGORY_SPORTS, " (體育) ");
    }

    @Inject
    HeadlineClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @NonNull
    protected List<NewsItem> filter(@NonNull final String url, @NonNull final RssFeed feed) {
        if (feed.getItems() == null) return new ArrayList<>();

        final String        keyword  = HeadlineClient.KEYWORDS.get(url.substring(HeadlineClient.URL.length()));
        final List<RssItem> rssItems = new ArrayList<>();

        for (final RssItem item : feed.getItems()) {
            final int index = item.getTitle().indexOf(keyword);

            if (index >= 0) {
                item.setTitle(item.getTitle().substring(0, index));
                if (item.getEnclosure() != null) item.getEnclosure().setUrl(HeadlineClient.formatImageUrl(item.getEnclosure().getUrl()));

                rssItems.add(item);
            }
        }

        feed.getItems().clear();
        feed.getItems().addAll(rssItems);

        return super.filter(url, feed);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Single.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    final List<Image> images = new ArrayList<>();

                    for (final String imageContainer : StringUtils.substringsBetween(html, "<a class=\"fancybox\" rel=\"gallery\"", "</a>")) {
                        final String imageUrl         = StringUtils.substringBetween(imageContainer, "href=\"", "\"");
                        final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"■", "\">");

                        if (imageUrl != null) images.add(new Image(HeadlineClient.formatImageUrl(imageUrl), imageDescription));
                    }

                    if (!images.isEmpty()) {
                        item.getImages().clear();
                        item.getImages().addAll(images);
                    }

                    final String[]      contents = StringUtils.substringsBetween(html, "<div id=\"news-content\" class=\"set-font-aera\" style=\"visibility: visible;\">", "</div>");
                    final StringBuilder builder  = new StringBuilder();

                    for (final String content : contents) builder.append(content);

                    item.setDescription(builder.toString());
                    item.setIsFullDescription(true);

                    if (!emitter.isDisposed()) emitter.onSuccess(item);
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), RxJava2Debug.getEnhancedStackTrace(error));

                    if (!emitter.isDisposed()) emitter.onSuccess(item);
                }
            ));
    }

    @NonNull
    private static String formatImageUrl(@NonNull final String url) {
        return url.startsWith("//") ? HeadlineClient.HTTP + url : url.startsWith(HeadlineClient.HTTP) ? url : HeadlineClient.IMAGE_URI + url;
    }
}

