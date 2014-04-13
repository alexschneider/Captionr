var initializePopcorn = function() {
    console.log(Popcorn);
    var wrapper = Popcorn.HTMLYouTubeVideoElement( '#videoContainer' );
    wrapper.src = "http://www.youtube.com/watch?v={{vid}}";
    var pop = Popcorn( wrapper )
    console.log(pop.parseVTT());
}

$( document ).ready(function() {
    initializePopcorn();
});