$(function() {
  $('.drawer').drawer();
  $('.drawer').on('click', function() {
    $('.drawer-nav').width($('.navbarCorner').width()-15);
  })
});
