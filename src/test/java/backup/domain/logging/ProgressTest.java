package backup.domain.logging;

import backup.domain.time.DefaultSystemTimeProvider;
import backup.domain.time.FixedSystemTimeProvider;
import backup.domain.time.SystemTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressTest {

    Progress sut;

    @Test
    void 進捗が10パーセント進むごとにincrementが現在の進捗ログテキストを返す() {
        sut = new Progress("test", 35L);

        assertThat(sut.increment()).describedAs("2.8%").isEmpty();
        assertThat(sut.increment()).describedAs("5.7%").isEmpty();
        assertThat(sut.increment()).describedAs("8.5%").isEmpty();
        assertThat(sut.increment()).describedAs("11.4%").hasValue("4/35 (11.4%)");

        assertThat(sut.increment()).describedAs("14.2%").isEmpty();
        assertThat(sut.increment()).describedAs("17.1%").isEmpty();
        assertThat(sut.increment()).describedAs("20.0%").hasValue("7/35 (20.0%)");

        assertThat(sut.increment()).describedAs("22.8%").isEmpty();
        assertThat(sut.increment()).describedAs("25.7%").isEmpty();
        assertThat(sut.increment()).describedAs("28.5%").isEmpty();
        assertThat(sut.increment()).describedAs("31.4%").hasValue("11/35 (31.4%)");
    }

    @Test
    void totalが0の場合() {
        sut = new Progress("test", 0L);

        assertThat(sut.currentProgress()).isEqualTo("0/0 (0.0%)");
    }
}