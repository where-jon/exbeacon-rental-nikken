$(function() {
  $('.drawer').drawer();
  $('.menu-title').on('click', function(){
      const openStateIcon = $(this).children('span.open-state').children('i');
      const isOpened = openStateIcon.is('.fa-angle-left');
      openStateIcon.removeClass().addClass(isOpened ? 'open-state fa fa-angle-down' : 'open-state fa fa-angle-left');
      $(this).next('ul').slideToggle();
      $(this).children('a').toggleClass('open');
  });
});