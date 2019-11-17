
package ch.monokellabs.heidelberger;

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

	private void cleanEndLinks(Element content) {
		content.getElementsByClass("content_fuss").forEach(Element::remove);
	}

}
