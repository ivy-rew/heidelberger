
package ch.monokellabs.heidelberger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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
		getBibleRefElements(content).forEach(bibRef -> {
			String[] refs = bibRef.ownText().split("/");
			Element refWrapper = asLinks(refs);
			bibRef.replaceWith(refWrapper);
		});
	}

	public List<String> getAllRefs()
	{
		return getBibleRefElements(heidelberger()).stream()
			.flatMap(refSection -> Arrays.stream(refSection.ownText().split("/")))
			.map(String::trim)
			.map(SwordRefParser::parseBibRef)
			.collect(Collectors.toList());
	}

	private Elements getBibleRefElements(Element content) {
		return content.getElementsByClass("content_def3");
	}

	private Element asLinks(String[] refs) {
		Element refWrapper = html.createElement("div");
		Arrays.stream(refs).forEach(refRaw ->
		{
			String verse = refRaw.trim();
			Element linked = html.createElement("a");
			linked.appendText(verse);
			linked.attr("href", externalBibleUriBase + verse);
			refWrapper.appendChild(linked);

			try {
				String parsed = SwordRefParser.parseBibRef(verse);
				String text = sword.getPlainText(parsed);
				Element cit = html.createElement("span");
				cit.appendText(text);
				refWrapper.appendChild(cit);
			} catch (BookException | NoSuchKeyException e) {
				e.printStackTrace();
			}
		});
		return refWrapper;
	}

	private final LocalSword sword = new LocalSword("GerSch");

	private void cleanEndLinks(Element content) {
		content.getElementsByClass("content_fuss").forEach(Element::remove);
	}

}
