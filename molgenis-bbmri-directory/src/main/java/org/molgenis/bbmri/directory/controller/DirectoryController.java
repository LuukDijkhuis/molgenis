package org.molgenis.bbmri.directory.controller;

import com.google.gson.Gson;
import org.molgenis.bbmri.directory.model.NegotiatorQuery;
import org.molgenis.bbmri.directory.settings.DirectorySettings;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.google.api.client.util.Lists.newArrayList;
import static com.google.api.client.util.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.bbmri.directory.controller.DirectoryController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Controller
@RequestMapping(URI + "/**")
public class DirectoryController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	public static final String ID = "bbmridirectory";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String VIEW_DIRECTORY = "view-directory";

	private final MenuReaderService menuReaderService;

	private static final String API_URI = "/api/";

	@Autowired
	public DirectoryController(MenuReaderService menuReaderService)
	{
		super(URI);
		this.menuReaderService = menuReaderService;
	}

	@Autowired
	Gson gson;

	@Autowired
	DirectorySettings settings;

	@Autowired
	MolgenisRSQL molgenisRSQL;

	@Autowired
	MetaDataService metaDataService;

	@RequestMapping()
	public String init(@RequestParam(required = false) String rSql, HttpServletRequest request, Model model)
	{
		if (rSql != null)
		{
			Query<Entity> query = molgenisRSQL
					.createQuery(rSql, metaDataService.getEntityType("eu_bbmri_eric_collections"));

			List<QueryRule> rules = query.getRules().get(0).getNestedRules();
			Map<String, Object> filters = newHashMap();
			List<Object> list = newArrayList();
			for (QueryRule rule : rules)
			{

				String field = rule.getField();
				// We only parse the booleans
				if (field != null)
				{
					list.add(rule.getValue());
					filters.put(field, list);
				}
			}

			// Use a hard coded mref for demo effect
			String materials = "[{operator : 'AND',value : [{id:'PLASMA',label:'Plasma'}, {id:'TISSUE_FROZEN',label:'Cryo tissue'}]},'OR',{value : { id : 'NAV', label : 'Not available' }}]";
			filters.put("materials", gson.fromJson(materials, List.class));
			model.addAttribute("filters", gson.toJson(filters));
		}

		model.addAttribute("username", getCurrentUsername());
		model.addAttribute("apiUrl", getApiUrl(request));
		model.addAttribute("baseUrl", getBaseUrl());
		return VIEW_DIRECTORY;
	}

	@RequestMapping("/query")
	@ResponseBody
	public String postQuery(@RequestBody NegotiatorQuery query) throws Exception
	{
		LOG.info("NegotiatorQuery " + query + " received, sending request");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		String username = settings.getString(DirectorySettings.USERNAME);
		String password = settings.getString(DirectorySettings.PASSWORD);
		headers.set("Authorization", this.generateBase64Authentication(username, password));
		HttpEntity entity = new HttpEntity(query, headers);

		LOG.trace("DirectorySettings.NEGOTIATOR_URL: [{}]", settings.getString(DirectorySettings.NEGOTIATOR_URL));
		return restTemplate.postForLocation(settings.getString(DirectorySettings.NEGOTIATOR_URL), entity)
				.toASCIIString();
	}

	/**
	 * Generate base64 authentication based on settings
	 *
	 * @return String
	 */
	public static String generateBase64Authentication(String username, String password)
	{
		requireNonNull(username, password);
		String userPass = username + ":" + password;
		String userPassBase64 = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
		return String.format("Basic %s", userPassBase64);
	}

	private static String getApiUrl(HttpServletRequest request)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + API_URI;
		}
		else
		{
			apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + API_URI;
		}
		return apiUrl;
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(DirectoryController.ID);
	}
}