
package ch.monokellabs.heidelberger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
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
		questionAsH3(content);
		cleanEndLinks(content);

		bibleRefs(content, new InlineResolvedFootnotes());

		cleanHkDef(content);
		content = asBody(content);
		return content.toString();
	}

	private Element heidelberger() {
		return html.getElementById("content_block_middle_text");
	}

	private Element asBody(Element wrapper) {
		Element body = html.createElement("body");
		Elements children = wrapper.getAllElements();
		children.remove(wrapper);
		children.forEach(child -> body.appendChild(child));
		wrapper.replaceWith(body);
		return body;
	}

	private void sonntagAsH2(Element content) {
		content.getElementsByClass("hk-def-1").forEach(sonntag -> {
			String title = sonntag.text().toString();
			Element titleElem = html.createElement("h2");
			titleElem.appendText(title);
			sonntag.replaceWith(titleElem);
		});
	}

	private void questionAsH3(Element content) {
		content.getElementsByClass("hk-def-2").forEach(question -> {
			String title = question.text().toString();
			if (title.startsWith("Frage"))
			{
				Element titleElem = html.createElement("h3");
				titleElem.appendText(title);
				question.replaceWith(titleElem);
			}
		});
	}

	private void cleanHkDef(Element content) {
		content.getElementsByClass("hk-def-3").forEach(this::cleanDef);
		content.getElementsByClass("hk-def-2").forEach(this::cleanDef);
	}

	private void cleanDef(Element hkDef3) {
		Element parent = hkDef3.parent();
		if (hkDef3.hasParent() && parent.normalName().startsWith("h"))
		{
			String title = hkDef3.text().toString();
			Element titleElem = html.createElement(parent.normalName());
			titleElem.appendText(title);
			hkDef3.replaceWith(titleElem);
		}
	}

	private void bibleRefs(Element content, RefHandler handler) {
		getBibleRefElements(content)
		.forEach(bibRef -> {
			Stream<String> refs = CatechismParser.splitRefNode(bibRef)
				.flatMap(CatechismParser::splitMultiRef)
				.filter(Objects::nonNull);
			handler.transform(bibRef, refs);
		});
	}

	@FunctionalInterface
	public static interface RefHandler
	{
		public void transform(Element bibRef, Stream<String> refs);
	}

	public List<String> getAllRefs()
	{
		List<String> allRefs = new ArrayList<>(850);
		for(Element refSection : getBibleRefElements(heidelberger()))
		{
			List<String> parsed = CatechismParser.splitRefNode(refSection)
			.flatMap(CatechismParser::splitMultiRef)
			.map(String::trim)
			.filter(StringUtils::isNotBlank)
			.map(ref -> {
				try
				{
					return SwordRef.parse(ref);
				} catch (IllegalArgumentException ex)
				{
					System.err.println("illegal ref in "+refSection+":\n"+ ex);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.map(SwordRef::enKey)
			.collect(Collectors.toList());

			allRefs.addAll(parsed);
		}
		return allRefs;
	}

	private static final Pattern CHAPTER_VERSE_ONLY = Pattern.compile("^[0-9]+, ?[0-9]+");

	public static Stream<String> splitRefNode(Element bibRef)
	{
		SwordRef last = null;
		String[] parts = bibRef.text().split("/");
		List<String> cleanedParts = new ArrayList<>();
		for(String part : parts)
		{
			part = part.trim();
			Matcher matcher = CHAPTER_VERSE_ONLY.matcher(part);
			if (matcher.find() && matcher.regionStart() == 0 && last != null)
			{
				String cleaned = last.getBook()+" "+part;
				System.out.println("cleaning '"+part+"' to: "+cleaned + " /in:"+bibRef);
				part = cleaned;
			}
			cleanedParts.add(part);
			last = SwordRef.parseOrNull(part);
		}
		return cleanedParts.stream();
	}

	private static final String QUEST20_MOSE_20 = "Mose 20";
	private static final String QUEST96_JES40 = "Jes; 40, 18-20.25";
	private static final String QUEST123_PS = "Ps 51, 20";

	public static Stream<String> splitMultiRef(String ref)
	{
		// unique anomalies::
		if (QUEST96_JES40.equals(ref))
		{ // only one known issue
			return Stream.of("Jes 40, 18-20", "Jes 40, 25");
		}
		else if (ref.startsWith(QUEST20_MOSE_20))
		{
			return Stream.of("2. "+ref);
		}
		else if (ref.startsWith(QUEST123_PS))
		{
			ref = ref.replace("51, 20", "51, 18");
		}

		if (ref.lastIndexOf(".") > 3 && !ref.startsWith("Das"))
		{
			String[] refs = new String[] { // does not scale: only one separator :-/
				StringUtils.substringBeforeLast(ref, "."),
				StringUtils.substringAfterLast(ref, ".")
			};
			SwordRef firstRef = SwordRef.parseOrNull(refs[0]);
			if (firstRef != null)
			{
				String prefix = firstRef.getBook()+" "+firstRef.chapter+",";
				return appendIfMissing(refs, prefix);
			}
		}
		else if (ref.contains(";"))
		{
			String[] refs = ref.split(";");
			SwordRef firstRef = SwordRef.parse(refs[0]);
			if (firstRef != null)
			{
				return appendIfMissing(refs, firstRef.getBook());
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

	private void cleanEndLinks(Element content) {
		content.getElementsByClass("content_fuss").forEach(Element::remove);
		content.getElementsByTag("a").forEach(Element::remove);
	}

}
