package ch.monokellabs.heidelberger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ImgLoaderTest {

	@Test
	@Disabled("verify dl with empty cache")
	public void canLoad() throws IOException, InterruptedException
	{
		File cacheDir = Files.createTempDirectory("myTEstDir").toFile();
		Assertions.assertThat(cacheDir).isEmptyDirectory();

		File img = CatechismLoader.getCatechismImage(cacheDir);
		Assertions.assertThat(img).exists();
		Assertions.assertThat(cacheDir).isNotEmptyDirectory();
	}

	@Test
	public void enforceCaching() throws IOException, InterruptedException
	{
		File cover = CatechismLoader.getCatechismImage();
		Assertions.assertThat(cover).isNotEmpty();

		Files.copy(cover.toPath(), new File("target", cover.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

}
