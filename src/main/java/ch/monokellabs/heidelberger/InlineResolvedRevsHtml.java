package ch.monokellabs.heidelberger;

import java.util.function.Function;
import java.util.stream.Stream;

import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.jsoup.nodes.Element;

import ch.monokellabs.heidelberger.CatechismParser.RefHandler;

public class InlineResolvedRevsHtml implements RefHandler
{
	private static final String externalBibleUriBase = "https://www.bibleserver.com/SLT/";

	private final LocalSword sword = new LocalSword("GerSch");
	private final Function<String, Element> nodeCreator;

	public InlineResolvedRevsHtml(Function<String, Element> nodeCreator)
	{
		this.nodeCreator = nodeCreator;
	}

	@Override
	public void transform(Element bibRef, Stream<String> refs) {
		Element refWrapper = asLinks(refs);
		bibRef.replaceWith(refWrapper);
	}

	private Element asLinks(Stream<String> refs) {
		Element refWrapper = nodeCreator.apply("div");
		refs.forEach(refRaw ->
		{
			String verse = refRaw.trim();
			Element linked = nodeCreator.apply("a");
			linked.appendText(verse);
			linked.attr("href", externalBibleUriBase + verse);
			refWrapper.appendChild(linked);

			try {
				SwordRef parsed = SwordRef.parse(verse);
				if (parsed != null)
				{
					String text = sword.getPlainText(parsed.enKey());
					Element cit = nodeCreator.apply("span");
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
}