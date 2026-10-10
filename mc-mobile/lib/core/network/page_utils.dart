/// Extrait la liste d'une réponse paginée Spring ({content: [...]}) ou d'une liste simple.
List<dynamic> pageItems(dynamic body) {
  if (body is Map && body['content'] is List) {
    return body['content'] as List<dynamic>;
  }
  if (body is List) return body;
  return const [];
}
