<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>SpigotBuyerCheck - ${userId}</title>
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
        <c:choose>
            <c:when test="${error == 1}">
                <div class='alert alert-danger tool-msg'>Spigot username not valid!</div>
            </c:when>
            <c:when test="${error == 2}">
                <div class='alert alert-danger tool-msg'>User is not found in any buyer list!<br>Last sync was: ${lastSyncFormatted}
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
            <h3>${username}</h3>

            <c:forEach items="${plugins}" var="plugin">
                <h4>${plugin.key.resourceName}</h4>
                <c:if test="${plugin.value.purchasePrice > 0}">
                    <b>Purchase price: </b>${plugin.value.purchaseCurrency} ${plugin.value.purchasePrice}</br>
                </c:if>
                <b>Purchase date: </b>${plugin.value.purchaseDateTime}</br>
            </c:forEach>
        </c:if>
    </div>
</div>

<script src="js/1.11.2.jquery.min.js"></script>
<script src="js/bootstrap.min.js"></script>
</body>
</html>
