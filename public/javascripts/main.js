/*jslint plusplus: true */
/*global console */
/*global $ */
/*global document */

function fillUsername() {
    var $user = $.cookie('geotagger-user');
    if (null == $user) {
        $user = 'Guest';
    }
    $('#visitor').text($user);
}

function userLogin() {
    $('#login-button').click(function (e) {
        var $user = $('#login-username').val();
        $.cookie('geotagger-user', $user);
        $('#visitor').text($user);
        $.unblockUI();
    });
    $.blockUI({
       message: $('#loginForm') 
    });
}

function checkUsername() {
    var $user = $.cookie('geotagger-user');
    if (null == $user) {
        userLogin();
    }
}

function doJudge(url, name, geotag, correct) {
    checkUsername();
    var $username = $('#visitor').text();
    var jsonData = {"url": url, "name": name, "geotag": geotag, "correct": correct, "username": $username};
    $.ajax({
        type: "POST",
        url: "judge",
        data: JSON.stringify(jsonData),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            alert(JSON.stringify(jsonData));
        }
    }); 
}

function bindAjaxEvents() {
    $('#loadingDiv').bind('ajaxStart', function() {
        this.show();
    }).bind('ajaxStop', function() {
        this.hide();
    });
}

function initBlockUI() {
    $.blockUI.defaults.css = { 
            padding: 0,
            margin: 0,
            width: '30%',
            top: '40%',
            left: '35%',
            textAlign: 'center',
            cursor: 'wait'
    };
}

function bindEnterKey() {
    $('input').keypress(function(e) {
        if(e.which == 13) {
            $(this).blur();
            $(this).parent().find('button').focus().click();
        }
    });
}

$(document).ready(function () {
//    bindAjaxEvents();
    //$.removeCookie('geotagger-user');
    bindEnterKey();
    initBlockUI();
    fillUsername();
    
    // set tags position
    var $tags = $('#tags');
    $('#tag-button').click(function (e) {
        var $url = $('#tag-url').val();
        
//        $.blockUI({ css: { 
//            border: 'none', 
//            padding: '15px', 
//            backgroundColor: '#000', 
//            '-webkit-border-radius': '10px', 
//            '-moz-border-radius': '10px', 
//            opacity: .5, 
//            color: '#fff' 
//        } });
        
        $.blockUI({
            message: $('#loadingDiv')
        });

        $.ajax({
            type: "GET",
            url: "geotags",
            data: {"url": $url},
            dataType: "json",
            complete: function() {
                $.unblockUI();
                $('#results').css({display: 'block'});
            },
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
                    $('#content').append(para);
                }
                
                console.log($tags.length + " tags.");
                for (i = 0; i < $tags.length; i++) {
                    var $li = $("<li id='result-" + i + "' class='clearfix' />");
                    var tagContent =
                        '<div class="row thumbnail">' +
                        '   <div class="span9 judge-area-left">' +
                        '        <h4>' +
                        '            <a href="#">' + $tags[i].name +'</a>' +
                        '        </h4>' +
                        '        <h4>' + $tags[i].geotag + '</h4>' +
                        '    </div>' +
                        '    <div class="span3 judge-area-right">' +
                        '        <div class="btn-group btn-group-vertical btn-group-justified pull-right" data-toggle="buttons-radio">' +
                        '            <button class="btn btn-success" result-index="' + i + '">Right</button>' +
                        '            <button class="btn btn-danger" result-index="' + i + '">Wrong</button>' +
                        '            <button class="btn btn-default" result-index="' + i + '">Maybe</button>' +
                        '        </div>' +
                        '    </div>' +
                        '</div>';

                    $li.append(tagContent);
                    $li.find('.btn-success').click(function(e) {
                        var index = $(this).attr('result-index');
                        doJudge($url, $tags[index].name, $tags[index].geotag, "1");
                        var $li1 = $('#result-' + index);
                        $li1.find('.btn-danger').hide();
                        $li1.find('.btn-default').hide();
                    });
                    $li.find('.btn-danger').click(function(e) {
                        var index = $(this).attr('result-index');
                        doJudge($url, $tags[index].name, $tags[index].geotag, "2");
                        var $li1 = $('#result-' + index);
                        $li1.find('.btn-success').hide();
                        $li1.find('.btn-default').hide();
                    });
                    $li.find('.btn-default').click(function(e) {
                        var index = $(this).attr('result-index');
                        doJudge($url, $tags[index].name, $tags[index].geotag, "3");
                        var $li1 = $('#result-' + index);
                        $li1.find('.btn-danger').hide();
                        $li1.find('.btn-success').hide();
                    });
                    $('#tags ul').append($li);
                }
            }
        });
    });
});