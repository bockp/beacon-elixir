package org.ega_archive.elixirbeacon.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.babelomics.csvs.lib.ws.QueryResponse;
import org.ega_archive.elixircore.exception.RestRuntimeException;
import org.ega_archive.elixircore.exception.ServerDownException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ParseResponse {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  public <T> QueryResponse<T> parseCsvsResponse(String url, Class<T> clazz) {
    return (QueryResponse<T>) parseResponse(url, QueryResponse.class, clazz);
  }

  public <T, U> Object parseResponse(String url, String token, Class<T> clazzT, Class<U> clazzU) {
    ResponseEntity response = null;
    try {
      MultiValueMap<String, String> header = null;
      if (StringUtils.isNotBlank(token)) {
        header = new LinkedMultiValueMap<>();
        header.add("token", token);
      }
      response = this.restTemplate
          .exchange(url, HttpMethod.GET, new HttpEntity(null, header), String.class, new Object[0]);
    } catch (ResourceAccessException ex) {
      throw new ServerDownException(ex.getMessage());
    }

    JavaType type = this.objectMapper.getTypeFactory()
        .constructParametricType(clazzT, new Class[]{clazzU});
    Object basicDTO = null;
    try {
      basicDTO = this.objectMapper.readValue((String) response.getBody(), type);
    } catch (IOException ex) {
      throw new RestRuntimeException("500",
          "Exception deserializing object: " + response.getBody() + "\n" + ex.getMessage());
    }
    log.debug("response: {}", basicDTO);
    return basicDTO;
  }

  public <T, U> Object parseResponse(String url, Class<T> clazzT, Class<U> clazzU) {
    return parseResponse(url, null, clazzT, clazzU);
  }

}
