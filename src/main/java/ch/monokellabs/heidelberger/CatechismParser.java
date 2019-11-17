
package ch.monokellabs.heidelberger;

import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class CatechismParser {

	private Document html;

	public CatechismParser(String html)
	{
		this.html = Jsoup.parse(html);
	}

	public String getMain()
	{
		Element content = heidelberger();
		sonntagAsH2(content);
		bibleRefs(content);
		cleanEndLinks(content);
		return content.toString();
	}

	private Element heidelberger() {
		return html.getElementById("content_block_middle_text");
	}

	private void sonntagAsH2(Element content) {
		content.getElementsByClass("hk-def-1").forEach(sonntag -> {
			String title = sonntag.ownText();
			Element titleElem = html.createElement("h2");
			titleElem.appendText(title);
			sonntag.replaceWith(titleElem);
		});
	}

	private static final String externalBibleUriBase = "https://www.bibleserver.com/SLT/";

	private void bibleRefs(Element content) {
		content.getElementsByClass("content_def3").forEach(bibRef -> {
			String[] refs = bibRef.ownText().split("/");
			Element refWrapper = asLinks(refs);
			bibRef.replaceWith(refWrapper);
		});
	}

	private Element asLinks(String[] refs) {
		Element refWrapper = html.createElement("div");
		Arrays.stream(refs).map(refRaw ->
		{
			String verse = refRaw.trim();
			Element linked = html.createElement("a");
			linked.appendText(verse);
			linked.attr("href", externalBibleUriBase + verse);
			return linked;
		})
		.forEach(link -> refWrapper.appendChild(link));
		return refWrapper;
	}

	private void cleanEndLinks(Element content) {
		content.getElementsByClass("content_fuss").forEach(Element::remove);
	}

}
