/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author Cristian.Villalba
 */
public class ColorHSL
{
   public static final void rgb2hsl(int[] rgb, float[] hsl)
   {
      float R = rgb[0] / 255.0f;
      float G = rgb[1] / 255.0f;
      float B = rgb[2] / 255.0f;

      float MAX = max(R, max(G, B));
      float MIN = min(R, min(G, B));
      float H, L, S;

      if (MIN == MAX)
         H = 0.0f;
      else if (R == MAX)
         H = 0.16666666f * ((G - B) / (MAX - MIN)) + 0.00000000f;
      else if (G == MAX)
         H = 0.16666666f * ((B - R) / (MAX - MIN)) + 0.33333333f;
      else
         H = 0.16666666f * ((R - G) / (MAX - MIN)) + 0.66666666f;

      L = 0.5f * (MIN + MAX);
      if (L == 0.0f || (MIN == MAX))
         S = 0.0f;
      else if (L <= 0.5f)
         S = (MAX - MIN) / (2 * L);
      else
         S = (MAX - MIN) / (2 - 2 * L);

      hsl[0] = absMod(H, 1.0f);
      hsl[1] = S;
      hsl[2] = L;
   }

   public static final int[] hsl2rgb(float[] hsl, int[] rgb)
   {
      float H = hsl[0];
      float S = hsl[1];
      float L = hsl[2];

      float R, G, B;

      if (S == 0.0f)
      {
         R = G = B = L;
      }
      else
      {
         float Q = (L < 0.5f) ? (L * (1.0f + S)) : ((L + S) - (L * S));
         float P = 2.0f * L - Q;
         float Hk = absMod(H, 1.0f);

         R = convert(Q, P, Hk + 0.33333333333f);
         G = convert(Q, P, Hk + 0.00000000000f);
         B = convert(Q, P, Hk - 0.33333333333f);
      }

      rgb[0] = (int) (clamp(R, 0.0f, 1.0f) * 255.0f);
      rgb[1] = (int) (clamp(G, 0.0f, 1.0f) * 255.0f);
      rgb[2] = (int) (clamp(B, 0.0f, 1.0f) * 255.0f);

      return rgb;
   }

   private static final float convert(float Q, float P, float Tx)
   {
      Tx = absMod(Tx, 1.0f);
      if (Tx < 1.0f / 6.0f)
         return P + ((Q - P) * 6.0f * Tx);
      if (Tx < 3.0f / 6.0f)
         return Q;
      if (Tx < 4.0f / 6.0f)
         return P + ((Q - P) * 6.0f * (4.0f / 6.0f - Tx));
      return P;
   }

   public static final float absMod(float val, float max)
   {
      return ((val % max) + max) % max;
   }

   public static final float clamp(float cur, float min, float max)
   {
      return (cur < min ? min : (cur > max ? max : cur));
   }
}