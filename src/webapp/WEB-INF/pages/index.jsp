<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>SpigotBuyerCheck</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/font-awesome.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <div class="content">
        <div id="logo"></div>
        <c:choose>
            <c:when test="${error == 1}">
                <div class='alert alert-danger tool-msg'>Spigot username not valid!</div>
            </c:when>
            <c:when test="${error == 2}">
                <div class='alert alert-danger tool-msg'>'<b>${username}</b>' is not found in any buyer list!<br>Last sync was: ${lastSyncFormatted}
                </div>
            </c:when>
            <c:when test="${error == 3}">
                <div class='alert alert-danger tool-msg'>Spigot username can not be empty!</div>
            </c:when>
            <c:when test="${error == 4}">
                <div class='alert alert-danger tool-msg'>The application is still starting ...</div>
            </c:when>
            <c:when test="${error == 5}">
                <div class='alert alert-danger tool-msg'>The application was unable to connect to Spigot! Please contact
                    the owner of the site!
                </div>
            </c:when>
        </c:choose>

        <c:if test="${success}">
            <div class='alert alert-success tool-msg'>The user '<a href="${userId}"><b>${username}</b></a>' is found in one or more plugin buyer
                lists<br>
                <c:forEach items="${plugins}" var="plugin">
                    <b>Plugin: </b>${plugin.key.resourceName}<br>
                </c:forEach>
            </div>
        </c:if>
        <div class="row">
            <div class="tool-form">
                <form class="form-horizontal" role="form" action="./" method="post">
                    <div class="form-group">
                        <div class="col-sm-10">
                            <input type="text" class="form-control" autocomplete="off" name="username" id="username"
                                   value="${inputUsername}"
                                   placeholder="Spigot Username">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-10">
                            <button type="submit" class="btn btn-primary">
                                <span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span> Check buyer
                            </button>
                            <button id="help" type="button" class="btn btn-default mobile-hide" data-container="body"
                                    data-toggle="popover" data-placement="right"
                                    data-content="Enter the Spigot username you want to check.">
                                <span class="glyphicon glyphicon-question-sign" aria-hidden="true"></span> Help
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="js/1.11.2.jquery.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/bootstrap-typeahead.min.js"></script>
<script>
    $(function () {
        $("[data-toggle='popover']").popover();
    });
    $("#username").typeahead({
        onSelect: function (item) {
            console.log(item);
        },
        ajax: {
            url: "api/user/fromName",
            timeout: 500,
            method: "get",
            preDispatch: function (query) {
                return {
                    q: query
                }
            },
            preProcess: function (data) {
                if (data.success === false) {
                    // Hide the list, there was some error
                    return false;
                }
                // We good!
                return data.matches;
            }
        }
    });
</script>
</body>
</html>
