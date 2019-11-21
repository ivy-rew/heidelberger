
package ch.monokellabs.heidelberger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CatechismParser {

	private final Document html;

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
		getBibleRefElements(content)
		.forEach(bibRef -> {
			Stream<String> refs = Arrays.stream(bibRef.ownText().split("/"))
				.flatMap(CatechismParser::splitMultiRef)
				.filter(Objects::nonNull);
			Element refWrapper = asLinks(refs);
			bibRef.replaceWith(refWrapper);
		});
	}

	public List<String> getAllRefs()
	{
		return getBibleRefElements(heidelberger()).stream()
			.flatMap(refSection -> Arrays.stream(refSection.ownText().split("/")))
			.flatMap(CatechismParser::splitMultiRef)
			.map(String::trim)
			.map(SwordRef::parse)
			.filter(Objects::nonNull)
			.map(SwordRef::enKey)
			.collect(Collectors.toList());
	}

	public static Stream<String> splitMultiRef(String ref)
	{
		if (ref.contains(";"))
		{
			String[] refs = ref.split(";");
			SwordRef firstRef = SwordRef.parse(refs[0]);
			if (firstRef != null)
			{
				return appendIfMissing(refs, firstRef.getBook());
			}
		}
		else if (ref.contains(".") && !ref.startsWith("Das"))
		{
			String[] refs = new String[] { // does not scale: only one separator :-/
				StringUtils.substringBeforeLast(ref, "."),
				StringUtils.substringAfterLast(ref, ".")
			};
			SwordRef firstRef = SwordRef.parse(refs[0]);
			if (firstRef != null)
			{
				String prefix = firstRef.getBook()+" "+firstRef.chapter+",";
				return appendIfMissing(refs, prefix);
			}
		}
		return Stream.of(ref);
	}

	private static Stream<String> appendIfMissing(String[] refs, String prefix) {
		return Arrays.stream(refs)
			.map(abbRevRef -> {
				if (abbRevRef.contains(prefix))
				{
					return abbRevRef; // full rev (first)
				}
				StringBuilder fullRev = new StringBuilder();
				fullRev.append(prefix);
				if (!abbRevRef.startsWith(" "))
				{
					fullRev.append(" ");
				}
				return fullRev.append(abbRevRef).toString();
			});
	}

	private Elements getBibleRefElements(Element content) {
		return content.getElementsByClass("content_def3");
	}

	private Element asLinks(Stream<String> refs) {
		Element refWrapper = html.createElement("div");
		refs.forEach(refRaw ->
		{
			String verse = refRaw.trim();
			Element linked = html.createElement("a");
			linked.appendText(verse);
			linked.attr("href", externalBibleUriBase + verse);
			refWrapper.appendChild(linked);

			try {
				SwordRef parsed = SwordRef.parse(verse);
				if (parsed != null)
				{
					String text = sword.getPlainText(parsed.enKey());
					Element cit = html.createElement("span");
					cit.attr("style", "display:block");
					cit.appendText(text);
					refWrapper.appendChild(cit);
				}
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
