import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadRingBuffer<T> {
    private final T[] buffer;
    private final AtomicInteger head = new AtomicInteger(0);
    private final AtomicInteger tail = new AtomicInteger(0);
    private final ReentrantLock lock = new ReentrantLock();

    public MultiThreadRingBuffer(int capacityBuffer) {
        this.buffer = (T[]) new Object[capacityBuffer];
    }

    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            var nextHead = (head.getAndIncrement() + 1) % buffer.length;
            while (nextHead == tail.get()) {
                // если следующий элемент головы совпадает с хвостом, то ждем
                lock.wait();
            }
            buffer[nextHead] = item;
            lock.notifyAll();
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            var nextTail = (tail.getAndIncrement() + 1) % buffer.length;
            while (nextTail == head.get()) {
                // если следующий элемент хвоста совпадает с головой, то ждем
                lock.wait();
            }
            T item = buffer[nextTail];
            lock.notifyAll();
            return item;
        } finally {
            lock.unlock();
        }
    }
}