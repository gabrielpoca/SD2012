
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author gabrielpoca
 */
public class Lock {

    private ReentrantLock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();
    private boolean owner;
    private boolean wait;

    public Lock(boolean owner) {
        this.owner = owner;
        wait = false;
    }

    public void setOwnerTrue() {
        lock.lock();
        this.owner = true;
        cond.signal();
        lock.unlock();
    }

    public void setOwnerFalse() {
        lock.lock();
        this.owner = false;
        lock.unlock();
    }

    public boolean testOwner() {
        return wait;
    }

    public void lock() throws InterruptedException {
        lock.lock();
        wait = true;
        while(!owner) {
            cond.await();
        }
        wait = false;
        lock.unlock();
    }

    private void log(String s) {
        System.out.println(s);
    }
}
