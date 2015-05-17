package com.mridang.leecher;

import java.util.Date;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import com.mridang.widgets.BaseWidget;
import com.mridang.widgets.SavedSettings;
import com.mridang.widgets.WidgetHelpers;

/**
 * This class is the provider for the widget and updates the data
 */
public class LauncherWidget extends BaseWidget {

	/*
	 * @see com.mridang.widgets.BaseWidget#fetchContent(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings)
	 */
	@Override
	public String fetchContent(Context ctxContext, Integer intInstance, SavedSettings objSettings)
			throws Exception {

		// TODO: Use the GZIPped client
		JSONObject jsoData = new JSONObject();
		final Document docPage = Jsoup.connect("http://www.torrentleech.org/user/account/login/").data("username", objSettings.get("username")).data("login", "submit").data("password", objSettings.get("password")).method(Method.POST).followRedirects(true)
				.header("Referer", "http://www.torrentleech.org").header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:24.0) Gecko/20100101 Firefox/24.0").execute().parse();

		jsoData.put("ratio", docPage.select(".memberbar_alt").get(1).nextSibling().toString().replace("&nbsp;", "").trim());
		jsoData.put("uploaded", docPage.select(".uploaded").first().nextSibling().toString().replace("&nbsp;", "").trim().replace("B", "").replace(" ", ""));
		jsoData.put("downloaded", docPage.select(".downloaded").first().nextSibling().toString().replace("&nbsp;", "").trim().replace("B", "").replace(" ", ""));

		return jsoData.toString(2);

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getIcon()
	 */
	@Override
	public Integer getIcon() {

		return R.drawable.ic_notification;

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getKlass()
	 */
	@Override
	protected Class<?> getKlass() {

		return getClass();

	}

	/*
	 * @see com.mridang.BaseWidget#getToken()
	 */
	@Override
	public String getToken() {

		return "a1b2c3d4";

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#updateWidget(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings, java.lang.String)
	 */
	@Override
	public void updateWidget(Context ctxContext, Integer intInstance, SavedSettings objSettings, String strContent)
			throws Exception {

		final RemoteViews remView = new RemoteViews(ctxContext.getPackageName(), R.layout.widget);
		final JSONObject jsoData = new JSONObject(strContent);
		final ChartDrawer objCharter = new ChartDrawer(ctxContext);
		objCharter.execute(Float.parseFloat(jsoData.getString("ratio")));

		remView.setTextViewText(R.id.upload_amount, jsoData.getString("uploaded"));
		remView.setTextViewText(R.id.download_amount, jsoData.getString("downloaded"));
		remView.setTextViewText(R.id.torrent_ratio, jsoData.getString("ratio"));
		remView.setImageViewUri(R.id.progress_image, objCharter.get());

		final String strWebpage = "http://www.torrentleech.org/profile/" + objSettings.get("username");
		final PendingIntent pitOptions = WidgetHelpers.getIntent(ctxContext, WidgetSettings.class, intInstance);
		final PendingIntent pitWebpage = WidgetHelpers.getIntent(ctxContext, strWebpage);
		remView.setTextViewText(R.id.last_update, DateFormat.format("kk:mm", new Date()));
		remView.setOnClickPendingIntent(R.id.settings_button, pitOptions);
		//remView.setOnClickPendingIntent(R.id.widget_icon, pitWebpage);

		AppWidgetManager.getInstance(ctxContext).updateAppWidget(intInstance, remView);

	}

}