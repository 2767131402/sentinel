/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.TimeUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel demo.
 * <ul>
 * <li>Annotation usage see
 * https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81
 * </li>
 * <li>All usage see https://help.aliyun.com/document_detail/102546.html</li>
 * </ul>
 * <p>
 * To run the application, you have to give the license, eg
 * -Dproject.name=AppName -Dahas.license=xxx <br/>
 * Visit AHAS console https://ahas.console.aliyun.com to get license.
 *
 * </p>
 *
 * @author Eric Zhao
 * @author Carpenter Lee
 */
@SpringBootApplication
@Configuration
public class DemoApplication {
	@Autowired
	private DemoApplication userService;

	static AtomicLong total = new AtomicLong();
	static AtomicLong pass = new AtomicLong();
	static AtomicLong block = new AtomicLong();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public SentinelResourceAspect sentinelResourceAspect() {
		return new SentinelResourceAspect();
	}

	/**
	 * 加入{@link SentinelResource}注解的方法都会被Sentinel保护。
	 */
	@SentinelResource(blockHandler = "blockHandlerForGetUser")
	public User getUserById(Long id) throws Exception {
		total.incrementAndGet();
		pass.incrementAndGet();
		silentSleep(20);
		return new User(id, "XiaoMing");
	}

	/**
	 * blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用。方法名可以任意，
	 * 但是方法参数列表和返回值要跟原函数一致，并且最后一个参数是BlockException
	 */
	public User blockHandlerForGetUser(Long id, BlockException ex) {
		block.incrementAndGet();
		total.incrementAndGet();
		silentSleep(20);
		return new User(0L, "fallbackUser");
	}

	private void doSomething() {
		try (Entry entry = SphU.entry("doSomething")) {
			pass.incrementAndGet();
			doAnotherThing();
		} catch (BlockException ex) {
			block.incrementAndGet();
		} finally {
			total.incrementAndGet();
		}
	}

	private void doAnotherThing() {
		try (Entry entry = SphU.entry("doAnotherThing")) {
			pass.incrementAndGet();
			silentSleep(10);
		} catch (BlockException ex) {
			block.incrementAndGet();
		} finally {
			total.incrementAndGet();
		}
	}

	private void someFunction(String functionName, long sleepMills) {
		try (Entry entry = SphU.entry(functionName)) {
			pass.incrementAndGet();
			silentSleep(sleepMills);
		} catch (BlockException ex) {
			block.incrementAndGet();
		} finally {
			total.incrementAndGet();
		}
	}

	@PostConstruct
	public void autoRun() throws Exception {
		String msg = "###########################################################\n"
				+ "# AHAS Sentinel SDK demo running...\n"
				+ "# Please visit AHAS console https://ahas.console.aliyun.com\n" + "# to see your application\n"
				+ "###########################################################";
		System.out.println(msg);

		String function = "function_";
		for (int i = 0; i < 50; i++) {
			final String fname = function + i;
			final long sleepMills = (i + 1) * 10;
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						someFunction(fname, sleepMills);
					}
				}
			}).start();
		}

		/** 手动埋点演示 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 这里要注意初始化顺序
				silentSleep(1000);
				while (true) {
					Entry entry = null;
					try {
						entry = SphU.entry("custom-defined-resource", EntryType.IN);
						// token acquired, means pass
						pass.incrementAndGet();
						doSomething();
						silentSleep(10);
					} catch (BlockException e1) {
						block.incrementAndGet();
						silentSleep(20);
					} catch (Exception e2) {
						// biz exception
						e2.printStackTrace();
					} finally {
						total.incrementAndGet();
						if (entry != null) {
							entry.exit();
						}
					}
				}
			}
		}).start();
		/** 自动发起请求 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				silentSleep(1000);
				for (long i = 0;; i++) {
					try {
						User user = userService.getUserById(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		/** 打印每秒 QPS */
		new Thread(new Runnable() {
			@Override
			public void run() {
				long oldTotal = 0;
				long oldPass = 0;
				long oldBlock = 0;
				while (true) {
					silentSleep(1000);
					long globalTotal = total.get();
					long oneSecondTotal = globalTotal - oldTotal;
					oldTotal = globalTotal;
					long globalPass = pass.get();
					long oneSecondPass = globalPass - oldPass;
					oldPass = globalPass;
					long globalBlock = block.get();
					long oneSecondBlock = globalBlock - oldBlock;
					oldBlock = globalBlock;
					System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal + ", pass:"
							+ oneSecondPass + ", block:" + oneSecondBlock);
				}
			}
		}).start();
	}

	private void silentSleep(long ms) {
		try {
			TimeUnit.MILLISECONDS.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	static final class User {
		private Long id;
		private String name;

		public User(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "User{" + "id=" + id + ", name='" + name + '\'' + '}';
		}
	}
}
