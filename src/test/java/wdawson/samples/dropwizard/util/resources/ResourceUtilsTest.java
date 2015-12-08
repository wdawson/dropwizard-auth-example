package wdawson.samples.dropwizard.util.resources;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class ResourceUtilsTest {

    @Test
    public void testThatListOfStringsIsReadFromResource() {
        List<String> names = ResourceUtils.readLinesFromResource("fixtures/users/test-names.txt");

        assertThat(names).containsExactly("Jane Doe", "John Doe");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatListOfStringsThrowsExceptionWhenResourceNotFound() {
        ResourceUtils.readLinesFromResource("does-not-exist");
    }
}
