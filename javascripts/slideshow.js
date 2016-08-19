var slide_index = 0;
var slides = $("slide.show");

slides.removeClass("show");
slides.eq(slide_index).addClass("show");

function nextSlide()
{
	var index = Math.abs(slide_index % slides.length);

	slides.removeClass("show");
	slides.eq(index).addClass("show");

	slide_index++;
}

setInterval("nextSlide();", 5000);
