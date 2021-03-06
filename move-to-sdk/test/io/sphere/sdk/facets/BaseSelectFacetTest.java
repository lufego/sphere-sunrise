package io.sphere.sdk.facets;

import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.search.ProductProjectionSearchModel;
import io.sphere.sdk.search.TermFacetResult;
import io.sphere.sdk.search.TermStats;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BaseSelectFacetTest {
    private static final TermFacetResult FACET_RESULT_WITH_THREE_TERMS = TermFacetResult.of(5L, 60L, 0L, asList(
            TermStats.of("one", 30),
            TermStats.of("two", 20),
            TermStats.of("three", 10)));
    private static final List<String> SELECTED_VALUE_TWO = singletonList("two");
    private static final List<FacetOption> OPTIONS = asList(
            FacetOption.of("one", 30, false),
            FacetOption.of("two", 20, true),
            FacetOption.of("three", 10, false));

    @Test
    public void canBeDisplayedIfOverThreshold() throws Exception {
        final SelectFacet<ProductProjection> facet = selectFacetWithThreeOptions().threshold(3L).build();
        assertThat(facet.isAvailable()).isTrue();
    }

    @Test
    public void canNotBeDisplayedIfBelowThreshold() throws Exception {
        final SelectFacet<ProductProjection> facet = selectFacetWithThreeOptions().threshold(4L).build();
        assertThat(facet.isAvailable()).isFalse();
    }

    @Test
    public void optionsListIsTruncatedIfOverLimit() throws Exception {
        final SelectFacet<ProductProjection> facet = selectFacetWithThreeOptions()
                .selectedValues(SELECTED_VALUE_TWO)
                .limit(2L)
                .build();
        assertThat(facet.getLimitedOptions()).containsExactlyElementsOf(asList(OPTIONS.get(0), OPTIONS.get(1)));
        assertThat(facet.getAllOptions()).containsExactlyElementsOf(OPTIONS);
    }

    @Test
    public void optionsListIsNotTruncatedIfBelowLimit() throws Exception {
        final SelectFacet<ProductProjection> facet = selectFacetWithThreeOptions()
                .selectedValues(SELECTED_VALUE_TWO)
                .limit(3L)
                .build();
        assertThat(facet.getLimitedOptions()).containsExactlyElementsOf(OPTIONS);
        assertThat(facet.getAllOptions()).containsExactlyElementsOf(OPTIONS);
    }

    @Test
    public void throwsExceptionOnWrongThresholdAndLimit() throws Exception {
        final SelectFacetBuilder<ProductProjection> builder = selectFacetWithThreeOptions().threshold(10L);
        assertThatThrownBy(() -> {
            builder.limit(10L).build();
            builder.limit(3L).build();
        }).isExactlyInstanceOf(InvalidSelectFacetConstraintsException.class)
                .hasMessageContaining("Threshold: 10")
                .hasMessageContaining("Limit: 3");
    }

    private SelectFacetBuilder<ProductProjection> selectFacetWithThreeOptions() {
        return SelectFacetBuilder.of("foo", "bar", ProductProjectionSearchModel.of().categories().id())
                .facetResult(FACET_RESULT_WITH_THREE_TERMS);
    }
}
