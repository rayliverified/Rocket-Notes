"use strict";

/* ============== Page loader ============== */

window.onload = function(){
    $('.page-loader').fadeOut(500);
};

/* ============== Google maps ============== */

function initMap() {
    var uluru = {lat: -25.363, lng: 131.044};
    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 5,
        center: uluru
    });
    var marker = new google.maps.Marker({
        position: uluru,
        map: map
    });
};

(function($) {

    /* ============== Plugins ============== */

    /* Waves effect */
    Waves.init({
        duration : 400
    });
    Waves.attach('.btn', ['waves-button']);
    Waves.attach('ul.social-icons li a', ['waves-button', 'waves-light']);

    /* Summernote */
    $('[data-plugin="summernote"]').summernote({
        height : "200"
    });

    /* Typed.js */
    $('[data-plugin="typed-js"]').each(function(){
        var that = $(this),
            string = that.data("plugin-string");
        that.typed({
            strings: string,
            typeSpeed: 0,
            startDelay: 300,
            backDelay: 2000
        });
    });

    /* Bootstrap slider */
    $('[data-plugin="bootstrap-slider"]').each(function(){
        var that = $(this)
        that.bootstrapSlider({
            formatter: function(value) {
                return that.data("slider-tooltip-text") + value;
            }
        });
    });

    /* Owl carousel plugin */
    $('.owl-carousel').owlCarousel({
        loop: true,
        margin: 80,
        autoplay: true,
        autoplayTimeout: 2000,
        responsive: {
            0:{
                items:2
            },
            768:{
                items:3
            },
            992:{
                items:4
            },
            1200:{
                items:5
            }
        }
    });

    /* Bootstrap tooltip plugin */
    $('[data-toggle="tooltip"]').tooltip({
        container : "body"
    });

    /* ============== Other js ============== */

    $(document).on('click', '.accordion-group .accordion .accordion-title', function () {
        var that = $(this);
        that.closest(".accordion-group").find('.accordion').finish().removeClass("active", 100);
        that.parent(".accordion").finish().addClass("active", 100);
        return false;
    });

    /* Input file change */
    $(document).on('change', 'input[type=file]', function(event){
        var file = $(event.currentTarget)[0].files[0],
            that = $(this),
            reader = new FileReader(),
            fileTypes = ['jpeg', 'jpg', 'jpeg', 'png', 'gif'],
            extension = file.name.split('.').pop().toLowerCase(),
            preview_element = $(this).closest('.img-preview');
        if( fileTypes.indexOf(extension) == -1 ){
            if( $('.file-preview-error').length > 0 ){
                $('.file-preview-error').html("");
            }else{
                preview_element.after("<p class='text-red file-preview-error'>Please select image file</p>");
            }
        }else{
            reader.onload = function(e){
                var output = e.target.result;
                preview_element.css("background", "url(" + output + ")");
                $('.file-preview-error').remove();
            };
            reader.readAsDataURL(file);
        }
    });

    // Responsive button & overlay click
    $(document).on('click', 'nav.navbar ul.navbar-nav.responsive-btn a', function () {
        $('.navs').addClass('open animated slideInRight').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
            $(this).removeClass('animated slideInRight');
        });
        $('body').addClass("scroll-disabled").append('<div class="body-overlay"></div>');
        return false;
    }).on('click', '.body-overlay', function () {
        var that = $(this);
        $('.navs').addClass('animated slideOutRight').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
            $(this).removeClass('open animated slideOutRight');
            $('body').removeClass("scroll-disabled");
            that.remove();
        });
        return false;
    });

    var window_width = $(window).width();

    $(window).resize(function(){
        window_width = $(this).width();
    });

    // Responsive sidebar menu click
    $(document).on('click', 'nav.navbar ul.navbar-nav:not(.responsive-btn) li a', function () {
        if(window_width <= 992) {
            var that = $(this);
            that.parent().siblings().find('ul').hide(200);
            if (that.next('ul').length > 0) {
                that.next('ul').finish().slideToggle(200);
                return false;
            }
        }
    });

})(jQuery);