#!/usr/bin/env bash
#
# generar_markdown.sh
# ------------------------------------------------------------
# Recorre un proyecto y vuelca el contenido de los archivos
# .java, .xml y .properties en un único fichero Markdown,
# listo para usar como contexto/fuente para una IA.
#
# Excluye por defecto: node_modules, el módulo react indicado,
# carpetas de build (target, build, dist, out), .git y .idea.
#
# USO:
#   ./generar_markdown.sh [DIRECTORIO_PROYECTO] [ARCHIVO_SALIDA] [MODULO_REACT]
#
# Ejemplos:
#   ./generar_markdown.sh .
#   ./generar_markdown.sh /home/user/mi-proyecto salida.md frontend-react
#
# ------------------------------------------------------------

set -euo pipefail

# --- Parámetros ---------------------------------------------------------
PROYECTO_DIR="${1:-.}"
SALIDA="${2:-proyecto_fuentes.md}"
MODULO_REACT="${3:-}"   # nombre (o ruta relativa) del módulo react a excluir

# --- Validaciones --------------------------------------------------------
if [[ ! -d "$PROYECTO_DIR" ]]; then
    echo "Error: el directorio '$PROYECTO_DIR' no existe." >&2
    exit 1
fi

PROYECTO_DIR="$(cd "$PROYECTO_DIR" && pwd)"

echo "Analizando proyecto en: $PROYECTO_DIR"
echo "Archivo de salida: $SALIDA"
[[ -n "$MODULO_REACT" ]] && echo "Excluyendo módulo React: $MODULO_REACT"

# --- Construcción de exclusiones para 'find' ------------------------------
# Carpetas que siempre se excluyen
EXCLUIR_DIRS=(".git" ".idea" ".vscode" "node_modules" "target" "build" "dist" "out" ".mvn" ".gradle")

# Si se indicó el módulo react, se añade a la lista de exclusión
if [[ -n "$MODULO_REACT" ]]; then
    EXCLUIR_DIRS+=("$MODULO_REACT")
fi

# Construir expresión -path ... -prune para 'find'
PRUNE_EXPR=()
for dir in "${EXCLUIR_DIRS[@]}"; do
    PRUNE_EXPR+=(-path "*/${dir}" -o -path "*/${dir}/*" -o)
done
# quitar el último "-o" sobrante
unset 'PRUNE_EXPR[${#PRUNE_EXPR[@]}-1]'

# --- Cabecera del markdown -------------------------------------------------
{
    echo "# Fuente del proyecto: $(basename "$PROYECTO_DIR")"
    echo
    echo "> Generado automáticamente el $(date '+%Y-%m-%d %H:%M:%S')"
    echo "> Incluye archivos: \`.java\`, \`.xml\`, \`.properties\`"
    [[ -n "$MODULO_REACT" ]] && echo "> Excluido el módulo React: \`$MODULO_REACT\`"
    echo
    echo "---"
    echo
} > "$SALIDA"

# --- Función para determinar el lenguaje del bloque de código -------------
lenguaje_para() {
    case "$1" in
        *.java) echo "java" ;;
        *.xml)  echo "xml" ;;
        *.properties) echo "properties" ;;
        *) echo "" ;;
    esac
}

# --- Recorrido y volcado de archivos ---------------------------------------
TOTAL=0

# find con -prune para excluir carpetas, y luego filtrar por extensión
while IFS= read -r -d '' archivo; do
    RUTA_RELATIVA="${archivo#"$PROYECTO_DIR"/}"
    LANG="$(lenguaje_para "$archivo")"

    {
        echo "## \`$RUTA_RELATIVA\`"
        echo
        echo '```'"$LANG"
        cat "$archivo"
        echo '```'
        echo
    } >> "$SALIDA"

    TOTAL=$((TOTAL + 1))
done < <(find "$PROYECTO_DIR" \( "${PRUNE_EXPR[@]}" \) -prune -o \
            \( -name "*.java" -o -name "*.xml" -o -name "*.properties" \) \
            -type f -print0)

echo "Listo. Se incluyeron $TOTAL archivos en '$SALIDA'."
