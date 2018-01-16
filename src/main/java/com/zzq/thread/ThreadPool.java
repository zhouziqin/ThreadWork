package com.zzq.thread;

import java.util.LinkedList;
import java.util.List;
/**
 * 
 * @author zzq
 *
 */
public class ThreadPool {
	
	//默认工作线程
	private static int worker_num = 5;

	//工作线程
	private WorkThread[] workThreads;
	public static int getWorker_num() {
		return worker_num;
	}
	public WorkThread[] getWorkThreads() {
		return workThreads;
	}
	public static int getFinished_task() {
		return finished_task;
	}
	/**
	 * 获取任务个数
	 * @return
	 */
	public int getTaskQueueNumber() {
		return taskQueue.size();
	}
	//未处理任务
	private static volatile int finished_task = 0 ;
	//任务队列，作为一个缓冲/list线程不安全
	private List<Runnable> taskQueue = new LinkedList<Runnable>();
	
	private static ThreadPool threadPool;
	
	private ThreadPool(){
		this(5);
	}
	/**
	 * 初始化工作线程
	 * @param worker_num
	 */
	public ThreadPool(int worker_num){
		ThreadPool.worker_num = worker_num;
		workThreads = new WorkThread[worker_num];
		for(int i = 0 ; i < worker_num; i++){
			workThreads[i] = new WorkThread();
			workThreads[i].start();
		}
	}
	public static ThreadPool getThreadPool(){
		return getThreadPool(ThreadPool.worker_num);
	}
	private static ThreadPool getThreadPool(int worker_num1) {
		if(worker_num1<=0){
			worker_num1 = ThreadPool.worker_num;
			
		}
		if(threadPool == null){
			threadPool = new ThreadPool(worker_num1);
		}
 		return threadPool;
	}
	public void execute(Runnable task){
		synchronized (taskQueue) {
			taskQueue.add(task);
			taskQueue.notify();
		}
	}
	private void execute(Runnable[] task){
		synchronized(taskQueue){
			for(Runnable t : task){
				taskQueue.add(t);
				taskQueue.notify();
			}
		}
	}
	public void destory(){
		//等待任务执行结束
		while(!taskQueue.isEmpty()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i = 0 ; i<worker_num ;i++){
			workThreads[i].stopWorker();
			workThreads[i] = null;
		}
		threadPool = null;
		taskQueue.clear();
	}
	/**
	 * 工作线程
	 * @author zzq
	 *
	 */
	class WorkThread extends Thread{
		//工作线程是否有效
		private boolean isRunning = true;
		public void run() {
	 		Runnable r = null;
	 		while(isRunning){
	 			synchronized (taskQueue) {
					while(isRunning && taskQueue.isEmpty()){
						try {
							taskQueue.wait(20);
						} catch (InterruptedException e) {
 							e.printStackTrace();
						}
					}
					if(!taskQueue.isEmpty()){
						r = taskQueue.remove(0);
					}
					if(r!=null){
						r.run();//执行任务
					}
					finished_task++;
					r = null;
				}
	 			
	 		}
		}

		public void stopWorker() {
	 		isRunning = false;
		}

	}
}
