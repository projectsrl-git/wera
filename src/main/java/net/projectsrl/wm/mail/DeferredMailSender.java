
package net.projectsrl.wm.mail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.projectsrl.wm.mail.SendSMTPMail;

public class DeferredMailSender {

    private static DeferredMailSender           _instance = null;
    private static BlockingQueue<SendSMTPMail> _queue    = null;
    private static ThreadConsumer           _thread   = null;

    private class ThreadConsumer extends Thread {
        
        private Consumer _target;


        public ThreadConsumer(Consumer target) {
            super(target);
            _target=target;
        }

        public void exit() {
            _target.exit();
        }

    }

    private class Consumer implements Runnable {
        
        private boolean _isExit = false;
        
        public void run() {

            while (!_isExit) {
                try {
                    SendSMTPMail item=  _queue.take();
                    item.sendMail();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        
        public void exit() {
        
            _isExit = true;
        }
    }

    private DeferredMailSender() {

        _queue = new LinkedBlockingQueue<SendSMTPMail>();
        Consumer consumer = new Consumer();
        _thread = new ThreadConsumer(consumer);
        _thread.setDaemon(true);
        _thread.setName(this.getClass().getName());
    }

    public static DeferredMailSender getInstance() {

        if (_instance == null) {
            _instance = new DeferredMailSender();
        }

        return _instance;

    }

    public void start() {

        if (_thread != null && !_thread.isAlive()) {
            _thread.start();
        }
    }

    public void exit() {

        if (_thread != null && _thread.isAlive()) {
            _thread.exit();
        }
    }

    public BlockingQueue<SendSMTPMail> getQueue() {

        return _queue;
    }

    
    public boolean offer(SendSMTPMail item) {
        return _queue.offer(item);
    }    

    
}