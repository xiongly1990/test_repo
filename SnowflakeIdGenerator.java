import java.util.concurrent.locks.ReentrantLock;

/**
 * Twitter Snowflake ID 生成器
 *
 * 64位ID结构：
 * - 1 bit:   符号位，始终为0
 * - 41 bits: 时间戳（毫秒），可使用约69年
 * - 10 bits: 机器ID（5位数据中心 + 5位工作机器）
 * - 12 bits: 序列号，每毫秒最多生成4096个ID
 */
public class SnowflakeIdGenerator {

    // 时间戳起始点（2020-01-01），可自定义
    private final long epoch = 1577836800000L;

    // 各部分位数
    private final long workerIdBits = 5L;
    private final long dataCenterIdBits = 5L;
    private final long sequenceBits = 12L;

    // 各部分最大值
    private final long maxWorkerId = ~(-1L << workerIdBits);   // 31
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits); // 31
    private final long maxSequence = ~(-1L << sequenceBits);  // 4095

    // 各部分向左偏移量
    private final long workerIdShift = sequenceBits;
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    // 时间戳增量
    private final long timestampIncrement = 1L << timestampLeftShift;

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // 并发控制
    private final ReentrantLock lock = new ReentrantLock();

    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围: 0-" + maxWorkerId);
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 超出范围: 0-" + maxDataCenterId);
        }

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成下一个ID
     */
    public long nextId() {
        lock.lock();
        try {
            long timestamp = timeGen();

            // 时钟回拨检测
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("时钟回拨，停止生成ID。差值: " + (lastTimestamp - timestamp) + "ms");
            }

            // 同一毫秒内，序列号递增
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & maxSequence;
                // 序列号溢出，等待下一毫秒
                if (sequence == 0) {
                    timestamp = waitUntilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - epoch) << timestampLeftShift)
                    | (dataCenterId << dataCenterIdShift)
                    | (workerId << workerIdShift)
                    | sequence;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒，直到时间戳前进
     */
    protected long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 从ID中解析时间戳
     */
    public long getTimestampFromId(long id) {
        return ((id >> timestampLeftShift) & (~(-1L << 41))) + epoch;
    }

    /**
     * 从ID中解析数据中心ID
     */
    public long getDataCenterIdFromId(long id) {
        return (id >> dataCenterIdShift) & maxDataCenterId;
    }

    /**
     * 从ID中解析工作机器ID
     */
    public long getWorkerIdFromId(long id) {
        return (id >> workerIdShift) & maxWorkerId;
    }

    /**
     * 从ID中解析序列号
     */
    public long getSequenceFromId(long id) {
        return id & maxSequence;
    }
}
