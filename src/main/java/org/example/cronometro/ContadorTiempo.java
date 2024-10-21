package org.example.cronometro;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class ContadorTiempo extends Application {
    private TextField inputField;
    private ProgressBar progressBar;
    private Label tiempoLabel;
    private Button iniciarButton, cancelarButton, guardarButton;
    private ComboBox<String> tiempoComboBox;
    private ToggleButton temaButton;
    private List<Integer> tiemposGuardados;  // Lista de tiempos predefinidos
    private int tiempoTotal, tiempoActual;
    private boolean contando;
    private boolean temaOscuro = false;
    private static final String ALERTA_SONIDO = "alerta.mp3";  // Archivo de sonido

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cronómetro con Tiempos Predefinidos");

        // Inicialización de la lista de tiempos guardados
        tiemposGuardados = new ArrayList<>();

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Etiqueta y campo para ingresar el tiempo
        Label instruccionLabel = new Label("Introduce el tiempo en segundos:");
        inputField = new TextField();

        // Barra de progreso
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(250);

        // Etiqueta para mostrar el tiempo restante
        tiempoLabel = new Label("Tiempo: 00:00:00");

        // Botones de control
        iniciarButton = new Button("Iniciar");
        cancelarButton = new Button("Cancelar");
        guardarButton = new Button("Guardar Tiempo");
        cancelarButton.setDisable(true);

        // ComboBox para mostrar tiempos predefinidos
        tiempoComboBox = new ComboBox<>();
        tiempoComboBox.setPromptText("Seleccionar tiempo guardado");
        tiempoComboBox.setOnAction(e -> cargarTiempoDesdeComboBox());

        // Botón para alternar entre tema oscuro y claro
        temaButton = new ToggleButton("Tema Oscuro");
        temaButton.setOnAction(e -> cambiarTema(root));

        // Agregar todos los componentes al contenedor
        root.getChildren().addAll(instruccionLabel, inputField, progressBar, tiempoLabel,
                iniciarButton, cancelarButton, guardarButton, tiempoComboBox, temaButton);

        // Eventos de los botones
        iniciarButton.setOnAction(e -> iniciarContador());
        cancelarButton.setOnAction(e -> cancelarContador());
        guardarButton.setOnAction(e -> guardarTiempo());

        // Configurar y mostrar la escena
        Scene scene = new Scene(root, 350, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void iniciarContador() {
        try {
            // Validar entrada del usuario
            tiempoTotal = Integer.parseInt(inputField.getText());
            if (tiempoTotal <= 0) throw new NumberFormatException();

            tiempoActual = 0;
            contando = true;
            progressBar.setProgress(0);
            iniciarButton.setDisable(true);
            cancelarButton.setDisable(false);
            inputField.setDisable(true);

            // Hilo para controlar el conteo del tiempo
            new Thread(() -> {
                while (contando && tiempoActual < tiempoTotal) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    tiempoActual++;
                    Platform.runLater(this::actualizarUI);
                }

                if (tiempoActual >= tiempoTotal) {
                    Platform.runLater(() -> {
                        mostrarAlertaFinalizacion();
                        reiniciarUI();
                    });
                }
            }).start();
        } catch (NumberFormatException ex) {
            mostrarAlertaError("Por favor, introduce un número válido mayor que cero.");
        }
    }

    private void cancelarContador() {
        contando = false;
        reiniciarUI();
    }

    private void actualizarUI() {
        double progreso = (double) tiempoActual / tiempoTotal;
        progressBar.setProgress(progreso);
        tiempoLabel.setText(formatoTiempo(tiempoActual));
    }

    private void reiniciarUI() {
        iniciarButton.setDisable(false);
        cancelarButton.setDisable(true);
        inputField.setDisable(false);
        progressBar.setProgress(0);
        tiempoLabel.setText("Tiempo: 00:00:00");
    }

    private String formatoTiempo(int segundosTotales) {
        int horas = segundosTotales / 3600;
        int minutos = (segundosTotales % 3600) / 60;
        int segundos = segundosTotales % 60;
        return String.format("Tiempo: %02d:%02d:%02d", horas, minutos, segundos);
    }


    private void mostrarAlertaFinalizacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tiempo completado");
        alert.setHeaderText(null);
        alert.setContentText("¡El tiempo ha finalizado!");
        alert.showAndWait();
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void guardarTiempo() {
        try {
            int tiempo = Integer.parseInt(inputField.getText());
            if (tiempo <= 0) throw new NumberFormatException();

            tiemposGuardados.add(Integer.valueOf(tiempo));
            tiempoComboBox.getItems().add(tiempo + " segundos");
            inputField.clear();
        } catch (NumberFormatException ex) {
            mostrarAlertaError("Por favor, introduce un número válido para guardar.");
        }
    }

    private void cargarTiempoDesdeComboBox() {
        String seleccion = tiempoComboBox.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            String[] partes = seleccion.split(" ");
            inputField.setText(partes[0]);
        }
    }

    private void cambiarTema(VBox root) {
        temaOscuro = !temaOscuro;
        if (temaOscuro) {
            root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
            root.getChildren().forEach(node -> node.setStyle("-fx-text-fill: white;"));
        } else {
            root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            root.getChildren().forEach(node -> node.setStyle("-fx-text-fill: black;"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
