package tomasulo;
import java.time.Duration;
import java.time.Instant;

public abstract class Timer
{
	public static final double CLOCKS_PER_SEC = 20;
	
	private static Instant _referencia = null;
	
	// Reinicia a referencia de contagem
	public static void reiniciarContador()
	{
		_referencia = Instant.now();
	}
	
	public static int tempoDecorrido()
	{
		if (_referencia == null)
		{
			return -1;
		}
		
		Instant agora = Instant.now();
		Duration tempoTotal = Duration.between(_referencia, agora);
		
		return (int) (tempoTotal.toMillis() * 0.001 * CLOCKS_PER_SEC);
	}
}