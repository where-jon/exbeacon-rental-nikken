$(function(){
    $('a.btn').on('touchstart touchend', function(e) {
        if (e.type === 'touchstart') {
          $(this).addClass('btnTappedClass');
        } else {
          $(this).removeClass('btnTappedClass');
        }
    });
});

function hasScrollBar(obj){
    return $(obj).get(0).scrollWidth > $(obj).width();
}