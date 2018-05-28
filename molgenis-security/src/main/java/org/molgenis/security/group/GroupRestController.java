package org.molgenis.security.group;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupService;
import org.molgenis.security.PermissionService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.PermissionSet.*;

@RestController
@Api("Group")
public class GroupRestController
{
	private final GroupValueFactory groupValueFactory;
	private final GroupService groupService;
	private final PermissionService permissionService;
	private static final Map<String, PermissionSet> DEFAULT_ROLES = ImmutableMap.of("Manager", WRITEMETA, "Editor",
			WRITE, "Viewer", READ);

	public GroupRestController(GroupValueFactory groupValueFactory, GroupService groupService,
			PermissionService permissionService)
	{
		this.groupValueFactory = requireNonNull(groupValueFactory);
		this.groupService = requireNonNull(groupService);
		this.permissionService = requireNonNull(permissionService);
	}

	@PostMapping("api/plugin/group")
	@ApiOperation(value = "Create a new Group", response = String.class)
	public String createGroup(
			@ApiParam("Alphanumeric name for the group, will be generated based on the label if not specified.") @RequestParam(name = "name", required = false) @Nullable String name,
			@ApiParam("Label for the group") @RequestParam("label") String label,
			@ApiParam("Description for the group") @RequestParam(name = "description", required = false) @Nullable String description,
			@ApiParam("Indication if this group should be publicly visible (not yet implemented!)") @RequestParam(name = "public", required = false, defaultValue = "true") boolean publiclyVisible)
	{
		GroupValue groupValue = groupValueFactory.createGroup(name, label, description, publiclyVisible,
				DEFAULT_ROLES.keySet());
		Group group = groupService.persist(groupValue);
		grantPermissions(group);
		return groupValue.getName();
	}

	private void grantPermissions(Group group)
	{
		PackageIdentity packageIdentity = new PackageIdentity(group.getRootPackage());
		group.getRoles()
			 .forEach(role -> permissionService.grant(packageIdentity, DEFAULT_ROLES.get(role.getName()), role));
	}

}
