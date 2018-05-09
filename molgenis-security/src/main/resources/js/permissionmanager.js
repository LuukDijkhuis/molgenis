(function ($, molgenis) {
    'use strict'

    $(function () {
        function createRolePermissionTable (data, permissions) {
            var items = []
            $.each(data.entityIds, function (entityId, entityTypeId) {
                if (data.rolePermissionMap && data.rolePermissionMap[entityId]) {
                    $.each(data.rolePermissionMap[entityId], function (idx, perm) {
                        items.push('<tr>')
                        items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>')
                        for (var i = 0; i < permissions.length; i++) {
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"' + (perm.type === permissions[i] ? ' checked' : '') + '></td>')
                        }
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>')
                        items.push('</tr>')
                    })
                } else {
                    items.push('<tr>')
                    items.push('<td>' + entityTypeId + '</td>')
                    for (var i = 0; i < permissions.length; i++) {
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"></td>')
                    }
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>')
                    items.push('</tr>')
                }
            })
            return items.join('')
        }

        function createUserPermissionTable (data, permissions) {
            var items = []
            $.each(data.entityIds, function (entityId, entityTypeId) {
                if (data.userPermissionMap && data.userPermissionMap[entityId]) {
                    $.each(data.userPermissionMap[entityId], function (idx, perm) {
                        items.push('<tr>')
                        items.push('<td>' + (idx == 0 ? entityTypeId : '') + '</td>')
                        for (var i = 0; i < permissions.length; i++) {
                            items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"' + (perm.type === permissions[i] ? ' checked' : '') + '></td>')
                        }
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="none"' + (perm.type ? '' : ' checked') + '></td>')
                    })
                } else {
                    items.push('<tr>')
                    items.push('<td>' + entityTypeId + '</td>')
                    for (var i = 0; i < permissions.length; i++) {
                        items.push('<td><input type="radio" name="radio-' + entityId + '" value="' + permissions[i] + '"></td>')
                    }
                    items.push('<td><input type="radio" name="radio-' + entityId + '" value="none" checked></td>')
                    items.push('</tr>')
                }
            })
            return items.join('')
        }

        $('#plugin-role-select').change(function () {
            $.get(molgenis.getContextUrl() + '/plugin/role/' + $(this).val(), function (data) {
                $('#plugin-role-permission-table tbody').empty().html(createRolePermissionTable(data, ['read']))
            })

        })
        $('#plugin-user-select').change(function () {
            $.get(molgenis.getContextUrl() + '/plugin/user/' + $(this).val(), function (data) {
                $('#plugin-user-permission-table tbody').empty().html(createUserPermissionTable(data, ['read']))
            })
        })
        $('#package-role-select').change(function () {
            $.get(molgenis.getContextUrl() + '/package/role/' + $(this).val(), function (data) {
                $('#package-role-permission-table tbody').empty().html(createRolePermissionTable(data, ['writemeta', 'write', 'read', 'count']))
            })
        })
        $('#package-user-select').change(function () {
            $.get(molgenis.getContextUrl() + '/package/user/' + $(this).val(), function (data) {
                $('#package-user-permission-table tbody').empty().html(createUserPermissionTable(data, ['writemeta', 'write', 'read', 'count']))
            })
        })
        $('#entity-class-role-select').change(function () {
            $.get(molgenis.getContextUrl() + '/entityclass/role/' + $(this).val(), function (data) {
                $('#entity-class-role-permission-table tbody').empty().html(createRolePermissionTable(data, ['writemeta', 'write', 'read', 'count']))
            })
        })
        $('#entity-class-user-select').change(function () {
            $.get(molgenis.getContextUrl() + '/entityclass/user/' + $(this).val(), function (data) {
                $('#entity-class-user-permission-table tbody').empty().html(createUserPermissionTable(data, ['writemeta', 'write', 'read', 'count']))
            })
        })
        $('input:checkbox', '#entity-type-rls-table').change(function () {
            var id = $(this).attr('id')
            var rlsEnabled = this.checked
            $.ajax({
                url: molgenis.getContextUrl() + '/update/entityclass/rls',
                type: 'POST',
                data: JSON.stringify({
                    id: id,
                    rlsEnabled: rlsEnabled
                }),
                contentType: 'application/json; charset=utf-8'
            })
        })

        $('#plugin-role-permission-form,' +
            '#plugin-user-permission-form,' +
            '#package-role-permission-form,' +
            '#package-user-permission-form,' +
            '#entity-class-role-permission-form,' +
            '#entity-class-user-permission-form').submit(function (e) {
            e.preventDefault()
            $.ajax({
                type: $(this).attr('method'),
                url: $(this).attr('action'),
                data: $(this).serialize(),
                success: function (data) {
                    $('#plugin-container .alert').remove()
                    $('#plugin-container').prepend('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> Updated plugin permissions</div>')

                    //Refresh
                    $('#plugin-role-select').change()
                    $('#package-role-select').change()
                    $('#entity-class-role-select').change()
                    $('#plugin-user-select').change()
                    $('#package-user-select').change()
                    $('#entity-class-user-select').change()
                },
                error: function (xhr, textStatus, errorThrown) {
                    var errorMessage = JSON.parse(xhr.responseText).errorMessage
                    $('#plugin-container .alert').remove()
                    $('#plugin-container').prepend('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> ' + errorMessage + '</div>')
                }
            })
        })

        $('a[data-toggle="tab"][href="#plugin-role-permission-manager"]').on('show.bs.tab', function (e) {
            $('#plugin-role-select').change()
        })
        $('a[data-toggle="tab"][href="#plugin-user-permission-manager"]').on('show.bs.tab', function (e) {
            $('#plugin-user-select').change()
        })
        $('a[data-toggle="tab"][href="#package-role-permission-manager"]').on('show.bs.tab', function (e) {
            $('#package-role-select').change()
        })
        $('a[data-toggle="tab"][href="#package-user-permission-manager"]').on('show.bs.tab', function (e) {
            $('#package-user-select').change()
        })
        $('a[data-toggle="tab"][href="#entity-class-role-permission-manager"]').on('show.bs.tab', function (e) {
            $('#entity-class-role-select').change()
        })
        $('a[data-toggle="tab"][href="#entity-class-user-permission-manager"]').on('show.bs.tab', function (e) {
            $('#entity-class-user-select').change()
        })

        $('#plugin-role-select').change()
        $('#package-role-select').change()
        $('#entity-class-role-select').change()
    })
}($, window.top.molgenis = window.top.molgenis || {}))