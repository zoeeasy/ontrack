var html = "";
$.each(releases, function (index, release) {
   html += "<tr>"
      + '<td><a href="https://github.com/nemerosa/ontrack/releases/tag/' + release + '">' + release + '</a></td>'
      + '<td><a href="release/' + release + '/doc/index.html">HTML</a> | <a href="release/' + release + '/index.pdf">PDF</a> | <a href="release/' + release + '/javadoc/index.html">Javadoc</a></td>'
      + "</tr>";
});
$('#ontrack-doc-body').html(html);
