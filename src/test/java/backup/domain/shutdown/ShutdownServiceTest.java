package backup.domain.shutdown;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ShutdownServiceTest {
    final CountDownLatch latch = new CountDownLatch(1);
    final MockShutdownExecutor executor = new MockShutdownExecutor(latch);
    final ShutdownService sut = new ShutdownService(executor, 1_000);

    @Test
    void permitが呼ばれている場合_待機時間が経過したらshutdownが実行される() throws Exception {
        sut.start();

        assertThat(latch.getCount()).isEqualTo(1);

        sut.permit();

        assertThat(latch.getCount()).isEqualTo(1);

        assertThat(latch.await(2_000, TimeUnit.MILLISECONDS)).describedAs("executor.shutdown is not called").isTrue();

        assertThat(executor.time).isGreaterThan(1_000);
    }

    @Test
    void permitが呼ばれていない場合_待機時間が経過してもshutdownは実行されない() throws Exception {
        sut.start();

        assertThat(latch.getCount()).isEqualTo(1);

        assertThat(latch.await(2_000, TimeUnit.MILLISECONDS)).isFalse();

        assertThat(executor.time).isEqualTo(-1L);
    }

    @Test
    void 待機時間経過後にpermitが呼ばれた場合は即座にshutdownが実行される() throws Exception {
        sut.start();

        assertThat(latch.getCount()).isEqualTo(1);

        assertThat(latch.await(2_000, TimeUnit.MILLISECONDS)).isFalse();

        assertThat(executor.time).isEqualTo(-1L);

        sut.permit();

        assertThat(latch.getCount()).isEqualTo(0);
    }

    static class MockShutdownExecutor implements ShutdownExecutor {
        private final long begin = System.currentTimeMillis();

        private final CountDownLatch latch;
        private long time = -1L;

        public MockShutdownExecutor(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void shutdown() {
            latch.countDown();
            time = System.currentTimeMillis() - begin;
        }
    }
}