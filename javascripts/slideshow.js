var slide_index = 0;
var slides = $("slide.show");

function nextSlide()
{
	var index = Math.abs(slide_index % slides.length);

	slides.removeClass("show");
	slides.eq(index).addClass("show");

	slide_index++;
}

nextSlide();
setInterval("nextSlide();", 5000);
