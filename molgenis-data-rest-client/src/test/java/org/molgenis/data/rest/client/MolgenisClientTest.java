package org.molgenis.data.rest.client;

import static com.google.common.collect.ImmutableList.of;

import com.google.gson.GsonBuilder;
import java.util.Map;
import org.molgenis.data.rest.client.bean.LoginResponse;
import org.molgenis.data.rest.client.bean.QueryResponse;
import org.molgenis.web.converter.MolgenisGsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class MolgenisClientTest {
  private MolgenisClient molgenis;

  @BeforeTest
  public void beforeTest() {
    RestTemplate template = new RestTemplate();
    template.setMessageConverters(
        of(new MolgenisGsonHttpMessageConverter(new GsonBuilder().setPrettyPrinting().create())));
    molgenis = new MolgenisClient(template, "http://localhost:8080/api/v1");
  }

  @Test(enabled = false)
  public void testLoginAndUpdate() {
    LoginResponse loginResponse = molgenis.login("admin", "secret");
    String token = loginResponse.getToken();
    QueryResponse queryResponse =
        molgenis.queryEquals(
            token, "RuntimeProperty", "Name", "plugin.dataexplorer.mod.aggregates");
    Map<String, Object> item = queryResponse.getItems().get(0);
    long id = Math.round((Double) item.get("id"));
    molgenis.update(token, "RuntimeProperty", Long.toString(id), "Value", "true");
  }
}
