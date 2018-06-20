$(function(){
    var duration = 300;
 
    var $sidebar = $('.sidebar');

    var sidebarBtn = document.getElementById("sidebarBtn")
    sidebarBtn.addEventListener('click', function() {
        $sidebar.toggleClass('open');
        if($sidebar.hasClass('open')){
            $sidebar.stop(true).animate({left: '-70px'}, duration, 'easeOutBack');
            /*$sidebarButton.find('span').text('CLOSE');*/
        }else{

            $sidebar.stop(true).animate({left: '-350px'}, duration, 'easeInBack');
            /*$sidebarButton.find('span').text('OPEN');*/
        };
    });
});