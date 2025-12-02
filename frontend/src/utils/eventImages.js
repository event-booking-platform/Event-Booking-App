
export const getEventImage = (event) => {
  const imageMap = {
    'CONCERT': '/images/events/concert.jpg',
    'MOVIE': '/images/events/movie.jpg',
    'THEATER': '/images/events/theater.jpg',
    'CONFERENCE': '/images/events/conference.jpg',
    'SPORTS': '/images/events/sports.jpg',
    'OTHER': '/images/events/event.jpg'
  };
  
  return imageMap[event.category] || '/images/events/event.jpg';
};

export const getEventImageWithFallback = (event, width = 300, height = 200) => {
  const mainImage = getEventImage(event);
  
  return {
    src: mainImage,
    fallback: `/images/events/event-fallback.jpg`,
    placeholder: `https://via.placeholder.com/${width}x${height}/667eea/ffffff?text=${encodeURIComponent(event.title)}`
  };
};