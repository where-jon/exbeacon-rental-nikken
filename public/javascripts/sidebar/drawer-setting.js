$(function() {
    $('.drawer').drawer();

    $('.drawer').on('click', function(ev) {
        $('.drawer-nav').width($('.navbarCorner').width());
    });

    $('.drawer').on('drawer.closed', function(){
        $('.drawer-nav').width(0);
    });
});
