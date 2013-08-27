/*jslint plusplus: true */
/*global console */
/*global $ */
/*global document */

$(document).ready(function () {
    $('#tag-button').click(function (e) {
        var $url = $('#tag-url').val(),
            $name = $('#tag-name').val();
        $.ajax({
            type: "GET",
            url: "geotags",
            data: {"url": $url, "name": $name},
            dataType: "json",
            success: function (data) {
                var $contents = data.content,
                    $tags = data.tags,
                    i;
                $("#content").html('');
                $("#tags ul").html('');
                
                console.log($contents.length + " sentences.");
                
                for (i = 0; i < $contents.length; i++) {
                    var para=document.createElement("p");
                    para.appendChild(document.createTextNode($contents[i]));
                    if (i == 0) {
                        console.log(para);
                    }
                    $('#content').append(para);
                }
                
                console.log($tags.length + " tags.");
                for (i = 0; i < $tags.length; i++) {
                    var $li = $("<li />");
                    $li.append("<div>" + $tags[i].name + "</div>");
                    $li.append("<div>" + $tags[i].geotag + "</div>");
                    if (i == 0) {
                        console.log($li);
                    }
                    $('#tags ul').append($li);
                }
            }
        });
    });
});