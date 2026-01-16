package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			try {
				switch (token) {
					case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
					case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
					case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
					case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd));
					default -> {
					}
				}
			} catch (ObjReaderException e) {
				throw new ObjReaderException("Line " + lineInd + ": " + e.getMessage(), lineInd);
			}
		}
		scanner.close();
		return result;
	}

	private static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() < 3) {
				throw new ObjReaderException("Too few vertex arguments. Expected 3, got " + wordsInLineWithoutToken.size(), lineInd);
			}

			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value: " + e.getMessage(), lineInd);
		}
	}

	private static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() < 2) {
				throw new ObjReaderException("Too few texture vertex arguments. Expected 2, got " + wordsInLineWithoutToken.size(), lineInd);
			}

			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value: " + e.getMessage(), lineInd);
		}
	}

	private static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() < 3) {
				throw new ObjReaderException("Too few normal arguments. Expected 3, got " + wordsInLineWithoutToken.size(), lineInd);
			}

			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value: " + e.getMessage(), lineInd);
		}
	}

	private static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Too few vertices in polygon. Minimum 3 required, got " + wordsInLineWithoutToken.size(), lineInd);
		}

		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
		}

		// Проверяем, что все массивы имеют одинаковый размер (если они не пустые)
		if (!onePolygonTextureVertexIndices.isEmpty() &&
				onePolygonTextureVertexIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Mismatched vertex and texture vertex indices in face", lineInd);
		}

		if (!onePolygonNormalIndices.isEmpty() &&
				onePolygonNormalIndices.size() != onePolygonVertexIndices.size()) {
			throw new ObjReaderException("Mismatched vertex and normal indices in face", lineInd);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		if (!onePolygonTextureVertexIndices.isEmpty()) {
			result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		}
		if (!onePolygonNormalIndices.isEmpty()) {
			result.setNormalIndices(onePolygonNormalIndices);
		}
		return result;
	}

	private static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd) {
		try {
			if (wordInLine == null || wordInLine.trim().isEmpty()) {
				throw new ObjReaderException("Empty face element", lineInd);
			}

			String[] wordIndices = wordInLine.split("/");

			// Проверяем индексы на отрицательные значения (относительные индексы)
			for (String indexStr : wordIndices) {
				if (!indexStr.isEmpty() && indexStr.startsWith("-")) {
					throw new ObjReaderException("Negative indices are not supported: " + wordInLine, lineInd);
				}
			}

			switch (wordIndices.length) {
				case 1 -> {
					// Формат: f v1 v2 v3
					int vertexIndex = Integer.parseInt(wordIndices[0]);
					if (vertexIndex == 0) {
						throw new ObjReaderException("Zero vertex index: " + wordInLine, lineInd);
					}
					onePolygonVertexIndices.add(Math.abs(vertexIndex) - 1);
				}
				case 2 -> {
					// Формат: f v1/vt1 v2/vt2 v3/vt3
					int vertexIndex = Integer.parseInt(wordIndices[0]);
					int textureIndex = Integer.parseInt(wordIndices[1]);

					if (vertexIndex == 0 || textureIndex == 0) {
						throw new ObjReaderException("Zero index in face element: " + wordInLine, lineInd);
					}

					onePolygonVertexIndices.add(Math.abs(vertexIndex) - 1);
					onePolygonTextureVertexIndices.add(Math.abs(textureIndex) - 1);
				}
				case 3 -> {
					// Форматы: f v1//vn1 v2//vn2 v3//vn3  или  f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
					int vertexIndex = Integer.parseInt(wordIndices[0]);
					int normalIndex = Integer.parseInt(wordIndices[2]);

					if (vertexIndex == 0 || normalIndex == 0) {
						throw new ObjReaderException("Zero index in face element: " + wordInLine, lineInd);
					}

					onePolygonVertexIndices.add(Math.abs(vertexIndex) - 1);
					onePolygonNormalIndices.add(Math.abs(normalIndex) - 1);

					if (!wordIndices[1].isEmpty()) {
						int textureIndex = Integer.parseInt(wordIndices[1]);
						if (textureIndex == 0) {
							throw new ObjReaderException("Zero texture index: " + wordInLine, lineInd);
						}
						onePolygonTextureVertexIndices.add(Math.abs(textureIndex) - 1);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid face element format: " + wordInLine, lineInd);
				}
			}

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse integer in face element: " + wordInLine, lineInd);
		}
	}
}