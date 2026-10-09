/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.internal.petra.executor;

import com.liferay.petra.concurrent.NoticeableFuture;
import com.liferay.petra.concurrent.NoticeableThreadPoolExecutor;
import com.liferay.petra.executor.PortalExecutorConfig;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Mariano Álvaro Sáiz
 */
public class WorkflowMetricsPortalExecutorTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		BundleContext bundleContext = Mockito.mock(BundleContext.class);

		ArgumentCaptor<PortalExecutorConfig> argumentCaptor =
			ArgumentCaptor.forClass(PortalExecutorConfig.class);

		Mockito.when(
			bundleContext.registerService(
				Mockito.eq(PortalExecutorConfig.class),
				argumentCaptor.capture(), Mockito.isNull())
		).thenReturn(
			Mockito.mock(ServiceRegistration.class)
		);

		PortalExecutorManager portalExecutorManager = Mockito.mock(
			PortalExecutorManager.class);

		Mockito.when(
			portalExecutorManager.getPortalExecutor(Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> {
				PortalExecutorConfig portalExecutorConfig =
					argumentCaptor.getValue();

				return new NoticeableThreadPoolExecutor(
					portalExecutorConfig.getCorePoolSize(),
					portalExecutorConfig.getMaxPoolSize(),
					portalExecutorConfig.getKeepAliveTime(),
					portalExecutorConfig.getTimeUnit(),
					new LinkedBlockingQueue<>(
						portalExecutorConfig.getMaxQueueSize()),
					portalExecutorConfig.getThreadFactory(),
					portalExecutorConfig.getRejectedExecutionHandler(),
					portalExecutorConfig.getThreadPoolHandler());
			}
		);

		ReflectionTestUtil.setFieldValue(
			_workflowMetricsPortalExecutor, "_portalExecutorManager",
			portalExecutorManager);

		_workflowMetricsPortalExecutor.activate(bundleContext);
	}

	@After
	public void tearDown() {
		_workflowMetricsPortalExecutor.deactivate();
	}

	@Test
	public void testAwait() throws Exception {

		// Await from another thread

		AtomicBoolean flag = new AtomicBoolean();

		_workflowMetricsPortalExecutor.execute(
			() -> {
				Thread.sleep(500);

				flag.set(true);
			});

		_workflowMetricsPortalExecutor.await();

		Assert.assertTrue(flag.get());

		// Await from the executor thread

		flag.set(false);

		NoticeableFuture<?> noticeableFuture =
			_workflowMetricsPortalExecutor.execute(
				() -> {
					_workflowMetricsPortalExecutor.await();

					flag.set(true);
				});

		noticeableFuture.get(1, TimeUnit.MINUTES);

		Assert.assertTrue(flag.get());
	}

	private final WorkflowMetricsPortalExecutor _workflowMetricsPortalExecutor =
		new WorkflowMetricsPortalExecutor();

}