var getYoutubeVideoID = function(url) {
    var regexPattern = /^(?:https?:\/\/)?(?:www\.)?(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))((\w|-){11})(?:\S+)?$/;
    return (url.match(regexPattern)) ? RegExp.$1 : false;
}

var handleInvalidYoutubeID = function() {
    $("#errorAlert").show();
    $('#videoTextDisplay').hide();
}

var handleSuccessYoutubeID = function(id) {
    $("#errorAlert").hide();
    var htm = '<iframe width="425" height="349" src="http://www.youtube.com/embed/' + id + '?rel=0" frameborder="0" allowfullscreen ></iframe>';
    $('#videoContainer').html(htm);
    $("#videoID").val(id);
    $('#videoTextDisplay').show();
}

$( document ).ready(function() {
    $( "#load" ).click(function() {
        var vidID = getYoutubeVideoID($("#youtubeLink").val());
        if(vidID) {
            handleSuccessYoutubeID(vidID);
        } else {
            handleInvalidYoutubeID();
        }
    });
});
