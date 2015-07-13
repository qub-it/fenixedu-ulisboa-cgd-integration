<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>
<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link href="//cdn.datatables.net/responsive/1.0.4/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="//cdn.datatables.net/responsive/1.0.4/js/dataTables.responsive.js"></script>
<link href="//cdn.datatables.net/tabletools/2.2.3/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="//cdn.datatables.net/tabletools/2.2.3/js/dataTables.tableTools.min.js"></script>
<link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/css/select2.min.css" rel="stylesheet" />
<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/js/select2.min.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.mifareManagement.readPersonMifare" />
		<small></small>
	</h1>
</div>

<%-- NAVIGATION --%>
<div class="well well-sm" style="display:inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/cgd/mifaremanagement/person/"  ><spring:message code="label.event.back" /></a>
|&nbsp;&nbsp;	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/cgd/mifaremanagement/person/readpersonmifare/${person.externalId}/createmifarecard"  ><spring:message code="label.event.mifareManagement.createMifareCard" /></a>	
</div>
	<c:if test="${not empty infoMessages}">
				<div class="alert alert-info" role="alert">
					
					<c:forEach items="${infoMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty warningMessages}">
				<div class="alert alert-warning" role="alert">
					
					<c:forEach items="${warningMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty errorMessages}">
				<div class="alert alert-danger" role="alert">
					
					<c:forEach items="${errorMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>

<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title"><spring:message code="label.details"/></h3>
	</div>
	<div class="panel-body">
<form method="post" class="form-horizontal">
<table class="table">
		<tbody>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Person.name"/></th> 
	<td>
		<c:out value='${person.name}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Person.username"/></th> 
	<td>
		<c:out value='${person.username}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Person.idDocumentNumber"/></th> 
	<td>
		<c:out value='${person.documentIdNumber}'/>
	</td> 
</tr>
</tbody>
</table>
</form>
</div>
</div>


<h2>
	<spring:message code="label.Person.cgdCardsSet"/>
</h2>

<c:choose>
	<c:when test="${not empty person.cgdCardsSet}">
		<table id="cardsTable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.CgdCard.mifareCode"/></th>
					<th><spring:message code="label.CgdCard.cardNumber"/></th>
					<th><spring:message code="label.CgdCard.issueDate"/></th>
					<th><spring:message code="label.CgdCard.validUntil"/></th>
					<th><spring:message code="label.CgdCard.temporary"/></th>
					<%-- Operations Column --%>
					<th></th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</c:when>
	<c:otherwise>
				<div class="alert alert-info" role="alert">
					<spring:message code="label.noResultsFound"/>
				</div>	
	</c:otherwise>
</c:choose>


<script>
	var cardsSet = [
			<c:forEach items="${person.cgdCardsSet}" var="card">
				{
				"DT_RowId" : '<c:out value='${card.externalId}'/>',
				"mifareCode" : "<c:out value='${card.mifareCode}'/>",
				"cardNumber" : "<c:out value='${card.cardNumber}'/>",
				"issueDate" : "<c:out value='${card.issueDate}'/>",
				"validUntil" : "<c:out value='${card.validUntil}'/>",
				"temporary" : "<spring:message code='label.${card.temporary}'/>",
				
				"actions" :
				" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/cgd/mifaremanagement/cgdcard/update/${card.externalId}\"><spring:message code='label.edit'/></a>" +
                "" },
            </c:forEach>
    ];
	
	$(document).ready(function() {


		var table = $('#cardsTable').DataTable({language : {
			url : "${datatablesI18NUrl}",			
		},
		"columns": [
			{ data: 'mifareCode' },
			{ data: 'cardNumber' },
			{ data: 'issueDate' },
			{ data: 'validUntil' },
			{ data: 'temporary' },
			{ data: 'actions' }
			
		],
		"data" : cardsSet,
		//Documentation: https://datatables.net/reference/option/dom
//"dom": '<"col-sm-6"l><"col-sm-3"f><"col-sm-3"T>rtip', //FilterBox = YES && ExportOptions = YES
"dom": 'T<"clear">lrtip', //FilterBox = NO && ExportOptions = YES
//"dom": '<"col-sm-6"l><"col-sm-6"f>rtip', //FilterBox = YES && ExportOptions = NO
//"dom": '<"col-sm-6"l>rtip', // FilterBox = NO && ExportOptions = NO
        "tableTools": {
            "sSwfPath": "//cdn.datatables.net/tabletools/2.2.3/swf/copy_csv_xls_pdf.swf"
        }
		});
		table.columns.adjust().draw();
	}); 
</script>