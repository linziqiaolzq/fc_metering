package com.example.demo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.example.demo.aliyun.Credentials;
import com.example.demo.aliyun.ExecuteContext;
import com.example.demo.service.AutoMeteringService;
import com.example.demo.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
@SpringBootApplication
@RestController
public class Application {

	@Autowired
	private AutoMeteringService autoMeteringService;

	private static final String REGION_ID = "regionId";

	private static final String SERVICE_ID = "serviceId";

	private static final String BILLING_DATE = "billingDate";

	private static final String REQUEST_ID = "x-fc-request-id";

	private static final String ACCESS_ID = "x-fc-access-key-id";

	private static final String ACCESS_SECRET = "x-fc-access-key-secret";

	private static final String SECURITY_TOKEN = "x-fc-security-token";

	private static final String SERVICE_VERSION = "serviceVersion";

	private static final String SUCCESS_COUNT = "successCount";

	private static final String TOTAL_COUNT = "totalCount";

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostMapping("/invoke")
	public String invoke(@RequestHeader Map<String, String> headers, @RequestBody(required = false) String payload) throws Exception {
		String requestId = headers.get(REQUEST_ID);
		System.out.printf("FC Invoke Start RequestId: %s%n", requestId);
		System.out.printf("-------------------------------------------------------------------- AutoMetering Start --------------------------------------------------------------------%n");
		String accessKeyId = headers.get(ACCESS_ID);
		String accessKeySecret = headers.get(ACCESS_SECRET);
		String securityToken = headers.get(SECURITY_TOKEN);

		Map<String, String> payloadMap = JsonUtil.parseObjectUpperCamelCase(payload, Map.class);
		String serviceId = payloadMap.get(SERVICE_ID);
		String regionId = payloadMap.get(REGION_ID);
		if (StringUtils.isBlank(serviceId) || StringUtils.isBlank(regionId)) {
			throw new RuntimeException("serviceId and regionId are mandatory for autoMetering");
		}
		String serviceVersion = payloadMap.get(SERVICE_VERSION);
		String billingDate = payloadMap.get(BILLING_DATE);
		if (StringUtils.isBlank(billingDate)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			billingDate = dateFormat.format(calendar.getTime());
		}

		ExecuteContext executeContext = new ExecuteContext();
		executeContext.setRegionId(regionId);
		executeContext.setCredentials(new Credentials(accessKeyId, accessKeySecret, securityToken));

		Map<String, Integer> resultMap = autoMeteringService.autoMetering(executeContext, serviceId, serviceVersion, billingDate);
		Integer successCount = resultMap.get(SUCCESS_COUNT);
		Integer totalCount = resultMap.get(TOTAL_COUNT);
		System.out.printf("-------------------------------------------------------------------- AutoMetering End --------------------------------------------------------------------%n");
		System.out.printf("FC Invoke End RequestId: %s%n", requestId);
		return "AutoMetering result: Pushed metering data for " + successCount +
				" serviceInstances. Found empty metering data for " + (totalCount - successCount) + " serviceInstances.";
	}
}