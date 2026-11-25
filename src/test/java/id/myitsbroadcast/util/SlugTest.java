package id.myitsbroadcast.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugTest {

    @Test
    void slugifiesCampusTitles() {
        assertEquals("pembukaan-pengisian-krs-semester-ganjil-2026-2027",
                Slug.slugify("Pembukaan Pengisian KRS Semester Ganjil 2026/2027"));
        assertEquals("open-recruitment-ukm-robotika-its-2026",
                Slug.slugify("Open Recruitment UKM Robotika ITS 2026"));
        assertEquals("ai-for-sustainable-maritime-industry",
                Slug.slugify("  AI for Sustainable Maritime Industry  "));
    }

    @Test
    void removesDiacriticsAndPunctuation() {
        assertEquals("kegiatan-kampus-tahun-baru", Slug.slugify("Kegiatan Kampus — Tahun Baru!"));
        assertEquals("cafe-its", Slug.slugify("Café ITS"));
    }

    @Test
    void fallsBackForBlankInput() {
        assertEquals("post", Slug.slugify(null));
        assertEquals("post", Slug.slugify("   "));
        assertEquals("post", Slug.slugify("!!!"));
    }

    @Test
    void appendsNumericSuffixUntilFree() {
        String unique = Slug.unique("Seminar AI", candidate ->
                candidate.equals("seminar-ai") || candidate.equals("seminar-ai-2"));

        assertEquals("seminar-ai-3", unique);
    }

    @Test
    void keepsSlugsWithinColumnLength() {
        String slug = Slug.slugify("a".repeat(260));

        assertEquals(180, slug.length());
    }
}
