/*
 * A ideia é criar um anel de peers. Cada um deles deve ler de um e escrever
 * para outro. Os peers devem receber porta de leitura e escrita tal como
 * uma variável que indique se é o dono do testemunho ou não.
 * 
 * A ordem de establecer os server sockets é relevante. A solução é cada
 * peer deve começar por criar o server socket mas apenas criar o channel
 * quando receber o testemunho pela primeira vez.
 */
package peer;

public class Peer {
    /**
     * Main thread.
     * @param args args[1] should be the read port. args[2] should be the 
     * write port. args[3] should be true or false telling if it is the owner
     * or not.
     */
    public static void main(String[] args) {
        try {
            Mutex mutex = new Mutex(Integer.valueOf(args[0]), Integer.valueOf(args[1]), args[2]);
            Thread thread_mutex = new Thread(mutex);
            thread_mutex.start();
            boolean run = true;
            while(run) {
                print("ENTER to lock mutex...");
                System.in.read();
                mutex.lock();
                
                // critic section
                //Thread.sleep(1000);
                
                
                print("ENTER to release mutex...");
                System.in.read();
                mutex.unlock();
                //Thread.sleep(2000);
                
            }
            thread_mutex.join();
        } catch (Exception e) {
        }
    }
    
    /**
     * Output string to console as IO.
     * @param s
     */
    public static void print(String s) {
        System.out.print(s);
    }
}
