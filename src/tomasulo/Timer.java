package tomasulo;
import java.time.Duration;
import java.time.Instant;

// Timer simples. Serve apenas para a contagem de clocks
public abstract class Timer
{
	// Define a taxa dos clocks
	public static double CLOCKS_PER_SEC = 1;
	
	private static Instant _referencia = null;
	
	// Reinicia a referencia de contagem
	public static void reiniciarContador()
	{
		_referencia = Instant.now();
	}
	
	// Tempo decorrido desde a referência (em clocks)
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
	
	//Tempo decorrido utilizando referência do start
	public static int tempoDecorrido(Instant startInst, int start)
	{
		if (_referencia == null)
		{
			return -1;
		}
		Instant agora = Instant.now();
		Duration tempoTotal = Duration.between(startInst, agora);
		
		return (int) (start + (tempoTotal.toMillis() * 0.001 * CLOCKS_PER_SEC));
	}
	
	public static void incClock(){
		if(CLOCKS_PER_SEC < 8){
			CLOCKS_PER_SEC = CLOCKS_PER_SEC*2;
		}
	}
	
	public static void decClock(){
		if(CLOCKS_PER_SEC > 0.125){
			CLOCKS_PER_SEC = CLOCKS_PER_SEC/2;
		}
	}
	
	public static double getClockPerSec(){
		return CLOCKS_PER_SEC;
	}
}