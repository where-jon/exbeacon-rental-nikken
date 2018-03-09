$(function(){
    $('a.btn').on('touchstart touchend', function(e) {
        if (e.type === 'touchstart') {
          $(this).addClass('btnTappedClass');
        } else {
          $(this).removeClass('btnTappedClass');
        }
    });
});